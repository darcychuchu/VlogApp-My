package com.vlog.my.screens.scripts.ebooks

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vlog.my.data.scripts.SubScriptsDataHelper
import com.vlog.my.data.scripts.ebooks.Ebook
import com.vlog.my.data.scripts.ebooks.EbookSqliteHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EbookListViewModel(
    application: Application,
    private val subScriptsDataHelper: SubScriptsDataHelper // Changed from EbookDao
) : AndroidViewModel(application) {

    private val _ebooks = MutableStateFlow<List<Ebook>>(emptyList())
    val ebooks: StateFlow<List<Ebook>> = _ebooks.asStateFlow()

    private var currentSubScriptIdLoaded: String? = null // Renamed for clarity

    fun loadEbooks(subScriptId: String) {
        if (subScriptId == currentSubScriptIdLoaded && _ebooks.value.isNotEmpty()) {
            // Data for this subScriptId is already loaded and non-empty, or a load is in progress.
            // To implement a refresh, you might remove the _ebooks.value.isNotEmpty() check
            // or add a specific refresh flag/method.
            Log.d("EbookListVM", "Ebooks for $subScriptId already loaded or load in progress.")
            return
        }
        currentSubScriptIdLoaded = subScriptId // Mark that we are attempting to load for this ID

        viewModelScope.launch {
            _ebooks.value = emptyList() // Clear previous ebooks or show loading state
            try {
                val subScript = withContext(Dispatchers.IO) {
                    subScriptsDataHelper.getUserScriptsById(subScriptId)
                }

                if (subScript?.databaseName != null) {
                    val dbHelper = EbookSqliteHelper(getApplication(), subScript.databaseName!!)
                    val loadedEbooks = withContext(Dispatchers.IO) {
                        dbHelper.getEbooksForSubScript(subScriptId)
                    }
                    _ebooks.value = loadedEbooks
                    Log.d("EbookListVM", "Loaded ${loadedEbooks.size} ebooks for $subScriptId from ${subScript.databaseName}")
                } else {
                    _ebooks.value = emptyList()
                    Log.e("EbookListVM", "SubScript with ID $subScriptId not found or has no databaseName.")
                }
            } catch (e: Exception) {
                _ebooks.value = emptyList() // Reset on error
                currentSubScriptIdLoaded = null // Allow reloading on next attempt
                Log.e("EbookListVM", "Error loading ebooks for $subScriptId: ${e.message}", e)
            }
        }
    }
}
