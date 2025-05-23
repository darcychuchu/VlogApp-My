package com.vlog.my.screens.bazaar

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vlog.my.data.Result
import com.vlog.my.data.bazaar.BazaarScriptsRepository
import com.vlog.my.data.scripts.SubScriptsDataHelper
import com.vlog.my.utils.FileRequestBodyFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

// Assuming ContentType.EBOOK.typeId is 4
private const val EBOOK_TYPE_ID = 4

class PublishToBazaarViewModel(
    application: Application,
    private val bazaarRepository: BazaarScriptsRepository,
    private val subScriptsDataHelper: SubScriptsDataHelper
) : AndroidViewModel(application) {

    private val _publishState = MutableStateFlow<Result<Any>?>(null)
    val publishState: StateFlow<Result<Any>?> = _publishState.asStateFlow()

    fun publishScript(
        subScriptId: String,
        title: String,
        description: String,
        tags: String,
        logoFilePath: String?,
        currentUserName: String, // Assuming this is passed or obtained from a user session ViewModel
        userToken: String // Assuming this is passed or obtained from a user session ViewModel
    ) {
        viewModelScope.launch {
            _publishState.value = Result.Loading
            try {
                val subScript = subScriptsDataHelper.getUserScriptsById(subScriptId)
                if (subScript == null) {
                    _publishState.value = Result.Error(Exception("SubScript not found with ID: $subScriptId"))
                    return@launch
                }

                // Prepare logo file part
                // Note: The problem description implies logoFilePath can be null.
                // The createScript API in BazaarScriptsService expects a non-null MultipartBody.Part for logoFile.
                // This might require a default logo or handling it as an error if logo is mandatory.
                // For now, proceeding as if logoFilePart can be null if logoFilePath is null,
                // but the API definition might need adjustment or a default logo needs to be used.
                val logoFilePart: MultipartBody.Part? = if (!logoFilePath.isNullOrBlank()) {
                    FileRequestBodyFactory.createFilePart(getApplication(), logoFilePath, "logoFile")
                } else {
                    // Handle missing logo: either error, default, or make it optional in API
                     _publishState.value = Result.Error(Exception("Logo file path is required."))
                     return@launch // Or, create a default part if API allows optional or has default
                }
                // If logoFilePart is null even after trying (e.g. file not found for a non-blank path)
                if (logoFilePart == null && !logoFilePath.isNullOrBlank()){
                     _publishState.value = Result.Error(Exception("Logo file not found or is invalid at path: $logoFilePath"))
                    return@launch
                }


                var databaseFilePart: MultipartBody.Part? = null
                val configsPayload: String

                if (subScript.isTyped == EBOOK_TYPE_ID) {
                    if (!subScript.databaseName.isNullOrBlank()) {
                        val dbPath = getApplication<Application>().getDatabasePath(subScript.databaseName!!).absolutePath
                        databaseFilePart = FileRequestBodyFactory.createFilePart(getApplication(), dbPath, "databaseFile")
                        if (databaseFilePart == null) {
                            _publishState.value = Result.Error(Exception("Ebook database file not found at path: $dbPath for SubScript ID: $subScriptId"))
                            return@launch
                        }
                    } else {
                        _publishState.value = Result.Error(Exception("Ebook script with ID: $subScriptId has no database name defined."))
                        return@launch
                    }
                    configsPayload = "{}"
                } else {
                    configsPayload = subScript.mappingConfig ?: "{}"
                }

                Log.d("PublishVM", "Submitting with: name=${subScript.name}, token=$userToken, title=$title, desc=$description, tags=$tags, configs=$configsPayload, type=${subScript.isTyped}, logoSet=${logoFilePart!=null}, dbSet=${databaseFilePart!=null}")

                val response = bazaarRepository.createScript(
                    name = subScript.name ?: "Unnamed Script", // Ensure name is not null
                    token = userToken,
                    logoFile = logoFilePart!!, // API expects non-null, error out above if null
                    title = title,
                    description = description,
                    tags = tags,
                    mappingConfig = configsPayload,
                    configTyped = subScript.isTyped,
                    databaseFile = databaseFilePart
                )
                // Assuming ApiResponse has a way to determine success/failure
                // For now, wrapping the raw response.
                 _publishState.value = Result.Success(response)

            } catch (e: Exception) {
                Log.e("PublishVM", "Error publishing script $subScriptId: ${e.message}", e)
                _publishState.value = Result.Error(e)
            }
        }
    }

    fun resetPublishState() {
        _publishState.value = null
    }
}

// You'll need a Result wrapper, e.g.:
// sealed class Result<out T> {
//     object Loading : Result<Nothing>()
//     data class Success<out T>(val data: T) : Result<T>()
//     data class Error(val exception: Exception) : Result<Nothing>()
// }
// And ensure your ApiResponse is compatible or handled appropriately.
