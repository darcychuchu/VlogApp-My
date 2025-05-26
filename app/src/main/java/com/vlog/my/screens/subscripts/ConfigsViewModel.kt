package com.vlog.my.screens.subscripts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vlog.my.data.scripts.configs.BasicsConfig
import com.vlog.my.data.scripts.configs.ConfigsDataHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ConfigsViewModel(application: Application) : AndroidViewModel(application) {

    private val dbHelper = ConfigsDataHelper(application)

    private val _configsList = MutableStateFlow<List<BasicsConfig>>(emptyList())
    val configsList: StateFlow<List<BasicsConfig>> = _configsList.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        fetchConfigs()
    }

    fun fetchConfigs() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _configsList.value = dbHelper.getAllBasicConfigs()
            } catch (e: Exception) {
                // Handle error, e.g., log it or show a message
                _configsList.value = emptyList() // Reset on error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteConfig(basicId: String) {
        viewModelScope.launch {
            try {
                dbHelper.deleteBasicConfig(basicId)
                // Refresh the list after deletion
                fetchConfigs()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
