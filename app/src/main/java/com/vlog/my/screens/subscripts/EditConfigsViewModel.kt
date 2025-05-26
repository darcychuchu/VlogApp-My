package com.vlog.my.screens.subscripts

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
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

// UIMetasConfig from AddConfigsViewModel can be reused or defined here if different
// For simplicity, let's assume it's similar. If not already in a shared file, define it:
// data class UIMetasConfig(
//     val existingMetaId: String? = null, // To track existing metas for update
//     val id: String = UUID.randomUUID().toString(), // For list key in UI
//     var metaTyped: Int = 0,
//     var metaKey: String = "",
//     var metaValue: String = ""
// )
// Using the one from AddConfigsViewModel for consistency:
// import com.vlog.my.screens.subscripts.UIMetasConfig

class EditConfigsViewModel(
    application: Application,
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
    val basicMetas = mutableStateListOf<UIMetasConfig>()

    // FieldsConfig fields
    val hasFieldsConfig = mutableStateOf(false)
    val fieldId = mutableStateOf<String?>(null) // To store existing fieldId
    val idField = mutableStateOf("")
    val titleField = mutableStateOf("")
    val picField = mutableStateOf("")
    val contentField = mutableStateOf("")
    val tagsField = mutableStateOf("")
    val sourceUrlField = mutableStateOf("")
    val fieldMetas = mutableStateListOf<UIMetasConfig>()

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
            // scriptsId is already hardcoded

            basicMetas.clear()
            config.metaList?.forEach { meta ->
                basicMetas.add(UIMetasConfig(id = meta.metaId, metaTyped = meta.metaTyped ?: 0, metaKey = meta.metaKey ?: "", metaValue = meta.metaValue ?: ""))
            }

            if (config.fieldsConfig != null) {
                hasFieldsConfig.value = true
                val fc = config.fieldsConfig
                fieldId.value = fc.fieldId // Store existing fieldId
                idField.value = fc.idField
                titleField.value = fc.titleField
                picField.value = fc.picField ?: ""
                contentField.value = fc.contentField ?: ""
                tagsField.value = fc.tagsField ?: ""
                sourceUrlField.value = fc.sourceUrlField ?: ""

                fieldMetas.clear()
                fc.metaList?.forEach { meta ->
                    fieldMetas.add(UIMetasConfig(id = meta.metaId, metaTyped = meta.metaTyped ?: 0, metaKey = meta.metaKey ?: "", metaValue = meta.metaValue ?: ""))
                }
            } else {
                hasFieldsConfig.value = false
            }
            _isLoading.value = false
        }
    }

    fun addBasicMeta() {
        basicMetas.add(UIMetasConfig(id = UUID.randomUUID().toString())) // New meta gets a new temp UI ID
    }

    fun removeBasicMeta(item: UIMetasConfig) {
        basicMetas.remove(item)
    }

    fun addFieldMeta() {
        fieldMetas.add(UIMetasConfig(id = UUID.randomUUID().toString())) // New meta gets a new temp UI ID
    }

    fun removeFieldMeta(item: UIMetasConfig) {
        fieldMetas.remove(item)
    }

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
                val currentBasicId = basicId.value // This is the ID of the config being edited

                val finalBasicMetas = basicMetas.map { uiMeta ->
                    MetasConfig(
                        metaId = if (dbHelper.getMetasConfigByQuoteId(currentBasicId).any { it.metaId == uiMeta.id }) uiMeta.id else UUID.randomUUID().toString(),
                        quoteId = currentBasicId,
                        metaTyped = uiMeta.metaTyped,
                        metaKey = uiMeta.metaKey.ifBlank { null },
                        metaValue = uiMeta.metaValue.ifBlank { null }
                    )
                }.toMutableList()

                var finalFieldsConfig: FieldsConfig? = null
                if (hasFieldsConfig.value) {
                    val currentFieldId = fieldId.value ?: UUID.randomUUID().toString() // Use existing or generate new if it was toggled on
                     if (fieldId.value == null) fieldId.value = currentFieldId // Persist newly generated ID if it was toggled

                    val finalFieldMetas = fieldMetas.map { uiMeta ->
                        MetasConfig(
                            metaId = if (dbHelper.getMetasConfigByQuoteId(currentFieldId).any { it.metaId == uiMeta.id }) uiMeta.id else UUID.randomUUID().toString(),
                            quoteId = currentFieldId,
                            metaTyped = uiMeta.metaTyped,
                            metaKey = uiMeta.metaKey.ifBlank { null },
                            metaValue = uiMeta.metaValue.ifBlank { null }
                        )
                    }.toMutableList()

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
                }

                val updatedConfig = BasicsConfig(
                    basicId = currentBasicId,
                    scriptsId = scriptsId.value, // Hardcoded
                    apiUrlField = apiUrlField.value,
                    urlParamsField = urlParamsField.value.ifBlank { null },
                    urlTypedField = urlTypedField.value,
                    rootPath = rootPath.value,
                    metaList = finalBasicMetas,
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
