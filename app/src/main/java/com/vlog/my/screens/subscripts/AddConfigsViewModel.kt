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

// Helper data class for MetasConfig UI state
data class UIMetasConfig(
    val id: String = UUID.randomUUID().toString(), // For list key
    var metaTyped: Int = 0,
    var metaKey: String = "",
    var metaValue: String = ""
)

class AddConfigsViewModel(application: Application) : AndroidViewModel(application) {

    private val dbHelper = ConfigsDataHelper(application)

    // BasicsConfig fields
    val apiUrlField = mutableStateOf("")
    val urlParamsField = mutableStateOf("")
    val urlTypedField = mutableStateOf(0) // Default to 0 (JSON)
    val rootPath = mutableStateOf("")
    val basicMetas = mutableStateListOf<UIMetasConfig>()

    // FieldsConfig fields
    val hasFieldsConfig = mutableStateOf(true) // Control if FieldsConfig section is visible/enabled
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
    val idFieldError = mutableStateOf<String?>(null) // For FieldsConfig.idField
    val titleFieldError = mutableStateOf<String?>(null) // For FieldsConfig.titleField


    private val _saveResult = MutableSharedFlow<Boolean>()
    val saveResult = _saveResult.asSharedFlow()

    fun addBasicMeta() {
        basicMetas.add(UIMetasConfig())
    }

    fun removeBasicMeta(item: UIMetasConfig) {
        basicMetas.remove(item)
    }

    fun addFieldMeta() {
        fieldMetas.add(UIMetasConfig())
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

    fun saveConfig() {
        if (!validateInputs()) {
            return
        }

        viewModelScope.launch {
            try {
                val basicId = UUID.randomUUID().toString()
                val fieldId = if (hasFieldsConfig.value) UUID.randomUUID().toString() else null

                val finalBasicMetas = basicMetas.map { uiMeta ->
                    MetasConfig(
                        metaId = UUID.randomUUID().toString(),
                        quoteId = basicId,
                        metaTyped = uiMeta.metaTyped,
                        metaKey = uiMeta.metaKey.ifBlank { null },
                        metaValue = uiMeta.metaValue.ifBlank { null }
                    )
                }.toMutableList()

                var finalFieldsConfig: FieldsConfig? = null
                if (hasFieldsConfig.value && fieldId != null) {
                    val finalFieldMetas = fieldMetas.map { uiMeta ->
                        MetasConfig(
                            metaId = UUID.randomUUID().toString(),
                            quoteId = fieldId, // Link to FieldsConfig's own ID
                            metaTyped = uiMeta.metaTyped,
                            metaKey = uiMeta.metaKey.ifBlank { null },
                            metaValue = uiMeta.metaValue.ifBlank { null }
                        )
                    }.toMutableList()

                    finalFieldsConfig = FieldsConfig(
                        fieldId = fieldId,
                        quoteId = fieldId, // quoteId for FieldsConfig itself, when it's the quote for its metas
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
                    basicId = basicId,
                    scriptsId = "test-script-id", // Hardcoded
                    apiUrlField = apiUrlField.value,
                    urlParamsField = urlParamsField.value.ifBlank { null },
                    urlTypedField = urlTypedField.value,
                    rootPath = rootPath.value,
                    metaList = finalBasicMetas,
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
