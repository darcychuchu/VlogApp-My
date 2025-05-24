package com.vlog.my.screens.scripts.ebooks

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vlog.my.data.scripts.SubScriptsDataHelper
import com.vlog.my.data.scripts.ebooks.FontSetting
import com.vlog.my.data.scripts.ebooks.EbookSqliteHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EbookSettingsViewModel(
    application: Application,
    private val subScriptsDataHelper: SubScriptsDataHelper // Changed from FontSettingDao
) : AndroidViewModel(application) {

    private val _currentFontSize = MutableStateFlow(16) // Default font size
    val currentFontSize: StateFlow<Int> = _currentFontSize.asStateFlow()

    private var currentDbNameForLoadedSettings: String? = null // Store dbName to avoid redundant loads for same DB
    private var lastLoadedSubScriptId: String? = null

    fun loadFontSize(subScriptId: String) {
        viewModelScope.launch {
            try {
                val subScript = withContext(Dispatchers.IO) {
                    subScriptsDataHelper.getUserScriptsById(subScriptId)
                }

                if (subScript?.databaseName != null) {
                    if (subScript.databaseName == currentDbNameForLoadedSettings && subScriptId == lastLoadedSubScriptId) {
                        Log.d("EbookSettingsVM", "Font size for $subScriptId (db: ${subScript.databaseName}) already loaded.")
                        return@launch
                    }

                    val dbHelper = EbookSqliteHelper(getApplication(), subScript.databaseName!!)
                    val fontSetting = withContext(Dispatchers.IO) {
                        dbHelper.getFontSetting(subScriptId) // Use subScriptId as key in font_settings table
                    }

                    if (fontSetting != null) {
                        _currentFontSize.value = fontSetting.fontSize
                        Log.d("EbookSettingsVM", "Loaded font size ${fontSetting.fontSize} for $subScriptId from ${subScript.databaseName}")
                    } else {
                        _currentFontSize.value = 16 // Reset to default if no setting found
                        Log.d("EbookSettingsVM", "No font setting for $subScriptId in ${subScript.databaseName}, using default 16.")
                    }
                    currentDbNameForLoadedSettings = subScript.databaseName
                    lastLoadedSubScriptId = subScriptId
                } else {
                    _currentFontSize.value = 16 // Default if no dbName
                    currentDbNameForLoadedSettings = null // Reset
                    lastLoadedSubScriptId = subScriptId // Still mark this attempt
                    Log.e("EbookSettingsVM", "SubScript $subScriptId not found or has no databaseName. Using default font size.")
                }
            } catch (e: Exception) {
                Log.e("EbookSettingsVM", "Error loading font size for $subScriptId: ${e.message}", e)
                _currentFontSize.value = 16 // Default on error
                currentDbNameForLoadedSettings = null // Allow reload attempt
            }
        }
    }

    fun updateFontSize(subScriptId: String, newSize: Int) {
        viewModelScope.launch {
            if (newSize <= 0) {
                Log.w("EbookSettingsVM", "Invalid font size requested: $newSize for $subScriptId. Not updating.")
                return@launch
            }

            try {
                val subScript = withContext(Dispatchers.IO) {
                    subScriptsDataHelper.getUserScriptsById(subScriptId)
                }

                if (subScript?.databaseName != null) {
                    val dbHelper = EbookSqliteHelper(getApplication(), subScript.databaseName!!)
                    val fontSetting = FontSetting(subScriptId = subScriptId, fontSize = newSize) // subScriptId is the key

                    withContext(Dispatchers.IO) {
                        dbHelper.addOrUpdateFontSetting(fontSetting)
                    }
                    _currentFontSize.value = newSize
                    currentDbNameForLoadedSettings = subScript.databaseName // Update this to reflect new state
                    lastLoadedSubScriptId = subScriptId
                    Log.d("EbookSettingsVM", "Updated font size to $newSize for $subScriptId in ${subScript.databaseName}")
                } else {
                    Log.e("EbookSettingsVM", "SubScript $subScriptId not found or has no databaseName. Cannot update font size.")
                }
            } catch (e: Exception) {
                Log.e("EbookSettingsVM", "Error updating font size for $subScriptId to $newSize: ${e.message}", e)
                // Optionally revert _currentFontSize or notify UI of error
            }
        }
    }
}
