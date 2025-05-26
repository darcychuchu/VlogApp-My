package com.vlog.my.screens.subscripts

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vlog.my.data.scripts.configs.BasicsConfig
import com.vlog.my.data.scripts.configs.ConfigsDataHelper
import com.vlog.my.data.scripts.configs.FieldsConfig
import com.vlog.my.data.scripts.configs.MetasConfig
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.UUID

// New data class for managing hierarchical MetasConfig in UI
data class EditableMetaItem(
    val clientSideId: String = UUID.randomUUID().toString(), // For UI list key and operations
    var metasConfig: MetasConfig, // Contains actual data: metaId, quoteId, metaKey, metaValue, metaTyped
    val children: MutableList<EditableMetaItem> = mutableStateListOf(), // For Compose reactivity
    var parentClientSideId: String? = null // To help reconstruct hierarchy, optional
)

class AddConfigsViewModel(application: Application) : AndroidViewModel(application) {

    private val dbHelper = ConfigsDataHelper(application)

    // BasicsConfig fields
    val apiUrlField = mutableStateOf("")
    val urlParamsField = mutableStateOf("")
    val urlTypedField = mutableStateOf(0) // Default to 0 (JSON)
    val rootPath = mutableStateOf("")
    // Use EditableMetaItem for basicMetas
    val basicEditableMetas = mutableStateListOf<EditableMetaItem>()


    // FieldsConfig fields
    val hasFieldsConfig = mutableStateOf(true) // Control if FieldsConfig section is visible/enabled
    val idField = mutableStateOf("")
    val titleField = mutableStateOf("")
    val picField = mutableStateOf("")
    val contentField = mutableStateOf("")
    val tagsField = mutableStateOf("")
    val sourceUrlField = mutableStateOf("")
    // Use EditableMetaItem for fieldMetas
    val fieldEditableMetas = mutableStateListOf<EditableMetaItem>()

    // Validation errors
    val apiUrlError = mutableStateOf<String?>(null)
    val rootPathError = mutableStateOf<String?>(null)
    val idFieldError = mutableStateOf<String?>(null) // For FieldsConfig.idField
    val titleFieldError = mutableStateOf<String?>(null) // For FieldsConfig.titleField


    private val _saveResult = MutableSharedFlow<Boolean>()
    val saveResult = _saveResult.asSharedFlow()

    // --- EditableMetaItem Management ---

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

    // Adds a new meta item. If parentClientSideId is null, adds to root list.
    fun addMetaItem(targetList: MutableList<EditableMetaItem>, parentClientSideId: String?, quoteIdProducer: () -> String) {
        val newItem = EditableMetaItem(
            metasConfig = MetasConfig(
                metaId = UUID.randomUUID().toString(), // This will be the actual DB metaId
                quoteId = quoteIdProducer(), // Set by the caller based on context (basic or field)
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
    
    // Removes a meta item. Searches recursively.
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

    // Update specific properties of a meta item identified by its clientSideId
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


    // --- Validation and Saving ---
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

    fun saveConfig() {
        if (!validateInputs()) {
            return
        }

        viewModelScope.launch {
            try {
                val newBasicId = UUID.randomUUID().toString()
                val newFieldId = if (hasFieldsConfig.value) UUID.randomUUID().toString() else null

                // Recursive function to convert EditableMetaItem hierarchy to MetasConfig hierarchy
                fun convertToMetasConfig(editableItems: List<EditableMetaItem>, quoteId: String): MutableList<MetasConfig>? {
                    if (editableItems.isEmpty()) return null
                    return editableItems.map { editableItem ->
                        editableItem.metasConfig.copy( // Use existing metaId from EditableMetaItem
                            quoteId = quoteId, // Ensure quoteId is correctly set from parent context
                            metaList = convertToMetasConfig(editableItem.children, quoteId)
                        )
                    }.toMutableList()
                }

                val finalBasicMetas = convertToMetasConfig(basicEditableMetas, newBasicId)

                var finalFieldsConfig: FieldsConfig? = null
                if (hasFieldsConfig.value && newFieldId != null) {
                    val finalFieldMetas = convertToMetasConfig(fieldEditableMetas, newFieldId)
                    finalFieldsConfig = FieldsConfig(
                        fieldId = newFieldId,
                        quoteId = newFieldId, 
                        idField = idField.value,
                        titleField = titleField.value,
                        picField = picField.value.ifBlank { null },
                        contentField = contentField.value.ifBlank { null },
                        tagsField = tagsField.value.ifBlank { null },
                        sourceUrlField = sourceUrlField.value.ifBlank { null },
                        metaList = finalFieldMetas
                    )
                }

                val newConfig = BasicsConfig(
                    basicId = newBasicId,
                    scriptsId = "test-script-id", // Hardcoded as per requirements
                    apiUrlField = apiUrlField.value,
                    urlParamsField = urlParamsField.value.ifBlank { null },
                    urlTypedField = urlTypedField.value,
                    rootPath = rootPath.value,
                    metaList = finalBasicMetas, // This is now recursively structured
                    fieldsConfig = finalFieldsConfig
                )

                dbHelper.insertBasicConfig(newConfig)
                _saveResult.emit(true)
            } catch (e: Exception) {
                // Log error e
                _saveResult.emit(false)
            }
        }
    }
}
