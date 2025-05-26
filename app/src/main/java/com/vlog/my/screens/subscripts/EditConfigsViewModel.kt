package com.vlog.my.screens.subscripts

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vlog.my.data.scripts.configs.BasicsConfig
import com.vlog.my.data.scripts.configs.ConfigsDataHelper
import com.vlog.my.data.scripts.configs.FieldsConfig
import com.vlog.my.data.scripts.configs.MetasConfig
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

// Re-using EditableMetaItem from AddConfigsViewModel.kt (assuming it's accessible or moved to a shared location)
// If not, it needs to be defined here or imported.
// For this context, we assume EditableMetaItem is available.

class EditConfigsViewModel(
    private val application: Application, // Added private val for context access
    private val basicIdToEdit: String
) : ViewModel() {

    private val dbHelper = ConfigsDataHelper(application)

    // State for loading indicator
    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _configNotFound = MutableStateFlow(false)
    val configNotFound = _configNotFound.asStateFlow()

    // BasicsConfig fields
    val basicId = mutableStateOf(basicIdToEdit) // Non-editable
    val scriptsId = mutableStateOf("test-script-id") // Hardcoded, non-editable
    val apiUrlField = mutableStateOf("")
    val urlParamsField = mutableStateOf("")
    val urlTypedField = mutableStateOf(0)
    val rootPath = mutableStateOf("")
    val basicEditableMetas = mutableStateListOf<EditableMetaItem>()

    // FieldsConfig fields
    val hasFieldsConfig = mutableStateOf(false)
    val fieldId = mutableStateOf<String?>(null) // To store existing fieldId
    val idField = mutableStateOf("")
    val titleField = mutableStateOf("")
    val picField = mutableStateOf("")
    val contentField = mutableStateOf("")
    val tagsField = mutableStateOf("")
    val sourceUrlField = mutableStateOf("")
    val fieldEditableMetas = mutableStateListOf<EditableMetaItem>()

    // Validation errors
    val apiUrlError = mutableStateOf<String?>(null)
    val rootPathError = mutableStateOf<String?>(null)
    val idFieldError = mutableStateOf<String?>(null)
    val titleFieldError = mutableStateOf<String?>(null)

    private val _updateResult = MutableSharedFlow<Boolean>()
    val updateResult = _updateResult.asSharedFlow()

    init {
        loadConfig()
    }
    
    // Recursive function to convert MetasConfig hierarchy to EditableMetaItem hierarchy
    private fun convertToEditableMetaItems(metas: List<MetasConfig>?): MutableList<EditableMetaItem> {
        if (metas == null) return mutableStateListOf()
        return metas.map { meta ->
            EditableMetaItem(
                clientSideId = UUID.randomUUID().toString(), // Fresh clientSideId for UI
                metasConfig = meta, // metaId here is the DB ID
                children = convertToEditableMetaItems(meta.metaList)
            )
        }.toMutableStateList()
    }


    private fun loadConfig() {
        viewModelScope.launch {
            _isLoading.value = true
            val config = dbHelper.getBasicConfigById(basicIdToEdit)
            if (config == null) {
                _configNotFound.value = true
                _isLoading.value = false
                return@launch
            }

            apiUrlField.value = config.apiUrlField
            urlParamsField.value = config.urlParamsField ?: ""
            urlTypedField.value = config.urlTypedField
            rootPath.value = config.rootPath
            // scriptsId is already hardcoded and non-editable.

            basicEditableMetas.clear()
            basicEditableMetas.addAll(convertToEditableMetaItems(config.metaList))

            if (config.fieldsConfig != null) {
                hasFieldsConfig.value = true
                val fc = config.fieldsConfig
                fieldId.value = fc.fieldId 
                idField.value = fc.idField
                titleField.value = fc.titleField
                picField.value = fc.picField ?: ""
                contentField.value = fc.contentField ?: ""
                tagsField.value = fc.tagsField ?: ""
                sourceUrlField.value = fc.sourceUrlField ?: ""

                fieldEditableMetas.clear()
                fieldEditableMetas.addAll(convertToEditableMetaItems(fc.metaList))
            } else {
                hasFieldsConfig.value = false
                fieldId.value = null
                fieldEditableMetas.clear()
            }
            _isLoading.value = false
        }
    }

    // --- EditableMetaItem Management (similar to AddConfigsViewModel) ---
    // These can be inherited from a base ViewModel if structure becomes complex
    private fun findEditableMetaRecursive(searchList: MutableList<EditableMetaItem>, clientSideId: String): EditableMetaItem? {
        for (item in searchList) {
            if (item.clientSideId == clientSideId) {
                return item
            }
            val foundInChildren = findEditableMetaRecursive(item.children, clientSideId)
            if (foundInChildren != null) {
                return foundInChildren
            }
        }
        return null
    }

    fun addMetaItem(targetList: MutableList<EditableMetaItem>, parentClientSideId: String?, quoteIdProducer: () -> String) {
        val newItem = EditableMetaItem(
            metasConfig = MetasConfig(
                metaId = UUID.randomUUID().toString(), // New DB metaId
                quoteId = quoteIdProducer(), 
                metaKey = "", metaValue = "", metaTyped = 0
            )
        )
        if (parentClientSideId == null) {
            targetList.add(newItem)
        } else {
            val parentItem = findEditableMetaRecursive(targetList, parentClientSideId)
            parentItem?.children?.add(newItem.apply { this.parentClientSideId = parentClientSideId })
        }
    }
    
    fun removeMetaItem(targetList: MutableList<EditableMetaItem>, itemToRemoveClientSideId: String): Boolean {
        val iterator = targetList.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            if (item.clientSideId == itemToRemoveClientSideId) {
                iterator.remove()
                return true
            }
            if (removeMetaItem(item.children, itemToRemoveClientSideId)) {
                return true
            }
        }
        return false
    }

    fun updateMetaItemProperties(
        targetList: MutableList<EditableMetaItem>,
        clientSideId: String,
        newKey: String,
        newValue: String,
        newType: Int
    ) {
        findEditableMetaRecursive(targetList, clientSideId)?.let { item ->
            item.metasConfig = item.metasConfig.copy(
                metaKey = newKey.ifBlank { null },
                metaValue = newValue.ifBlank { null },
                metaTyped = newType
            )
        }
    }
    // --- End EditableMetaItem Management ---

    private fun validateInputs(): Boolean {
        var isValid = true
        apiUrlError.value = if (apiUrlField.value.isBlank()) { isValid = false; "API URL cannot be empty" } else null
        rootPathError.value = if (rootPath.value.isBlank()) { isValid = false; "Root Path cannot be empty" } else null

        if (hasFieldsConfig.value) {
            idFieldError.value = if (idField.value.isBlank()) { isValid = false; "ID Field mapping cannot be empty" } else null
            titleFieldError.value = if (titleField.value.isBlank()) { isValid = false; "Title Field mapping cannot be empty" } else null
        } else {
            idFieldError.value = null
            titleFieldError.value = null
        }
        return isValid
    }

    fun updateConfig() {
        if (!validateInputs()) {
            return
        }

        viewModelScope.launch {
            try {
                val currentBasicId = basicId.value 

                // Recursive function to convert EditableMetaItem hierarchy back to MetasConfig hierarchy for saving
                // This ensures that existing metaIds are preserved if the meta was loaded from DB.
                // New metas will have UUIDs generated when EditableMetaItem was created.
                fun convertToMetasConfigForSave(editableItems: List<EditableMetaItem>, quoteId: String): MutableList<MetasConfig>? {
                    if (editableItems.isEmpty()) return null
                    return editableItems.map { editableItem ->
                        editableItem.metasConfig.copy( // metasConfig.metaId is the key part here
                            quoteId = quoteId,
                            metaList = convertToMetasConfigForSave(editableItem.children, quoteId)
                        )
                    }.toMutableList()
                }

                val finalBasicMetas = convertToMetasConfigForSave(basicEditableMetas, currentBasicId)
                
                var finalFieldsConfig: FieldsConfig? = null
                if (hasFieldsConfig.value) {
                    val currentFieldId = fieldId.value ?: UUID.randomUUID().toString() 
                    if (fieldId.value == null) { // If FieldsConfig was toggled on during edit
                        fieldId.value = currentFieldId 
                    }
                    val finalFieldMetas = convertToMetasConfigForSave(fieldEditableMetas, currentFieldId)
                    finalFieldsConfig = FieldsConfig(
                        fieldId = currentFieldId,
                        quoteId = currentFieldId, 
                        idField = idField.value,
                        titleField = titleField.value,
                        picField = picField.value.ifBlank { null },
                        contentField = contentField.value.ifBlank { null },
                        tagsField = tagsField.value.ifBlank { null },
                        sourceUrlField = sourceUrlField.value.ifBlank { null },
                        metaList = finalFieldMetas
                    )
                } else {
                    // If hasFieldsConfig is false, ensure we remove any existing FieldsConfig
                    // The updateBasicConfig in ConfigsDataHelper handles setting fieldId_fk to null
                    // and should also delete the orphaned FieldsConfig if it was previously associated.
                    // For safety, we could explicitly try to delete fieldId.value if it's not null.
                    // However, ConfigsDataHelper.updateBasicConfig should manage this based on null fieldsConfig.
                }

                val updatedConfig = BasicsConfig(
                    basicId = currentBasicId,
                    scriptsId = scriptsId.value, // Hardcoded, non-editable
                    apiUrlField = apiUrlField.value,
                    urlParamsField = urlParamsField.value.ifBlank { null },
                    urlTypedField = urlTypedField.value,
                    rootPath = rootPath.value,
                    metaList = finalBasicMetas, // Recursively structured
                    fieldsConfig = finalFieldsConfig
                )
                
                dbHelper.updateBasicConfig(updatedConfig)
                _updateResult.emit(true)
            } catch (e: Exception) {
                // Log error e
                _updateResult.emit(false)
            }
        }
    }
}

// ViewModelFactory for EditConfigsViewModel
class EditConfigsViewModelFactory(
    private val application: Application,
    private val basicId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditConfigsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EditConfigsViewModel(application, basicId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
