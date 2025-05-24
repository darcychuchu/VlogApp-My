package com.vlog.my.screens.videos

import android.app.Application
import android.content.ContentValues
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.vlog.my.data.scripts.videos.VideoMetadata
import com.vlog.my.data.scripts.videos.VideosScriptsDataHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.UUID

enum class ProcessingStatus {
    IDLE,
    ESTIMATING_SIZE,
    COMPRESSING,
    STORING,
    SUCCESS,
    ERROR
}

data class AddVideoUiState(
    val selectedVideoUri: Uri? = null,
    val selectedVideoName: String? = null,
    val processingStatus: ProcessingStatus = ProcessingStatus.IDLE,
    val originalSize: String = "N/A",
    val finalSize: String? = null,
    val errorMessage: String? = null,
    val videoTitle: String = ""
)

class AddVideoViewModel(
    private val application: Application,
    val databaseName: String // Passed via ViewModelFactory
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddVideoUiState())
    val uiState: StateFlow<AddVideoUiState> = _uiState.asStateFlow()

    private val workManager = WorkManager.getInstance(application)
    private var videosDataHelper: VideosScriptsDataHelper? = null

    init {
        // Initialize VideosScriptsDataHelper. This is a simplified approach.
        // In a real app with Hilt, this would be injected.
        // Or, if databaseName can change, initialize it when it's first needed.
        if (databaseName.isNotBlank()) {
            videosDataHelper = VideosScriptsDataHelper(application, databaseName)
        }
    }


    fun setSelectedVideo(uri: Uri?) {
        if (uri == null) {
            _uiState.value = _uiState.value.copy(
                selectedVideoUri = null,
                selectedVideoName = null,
                processingStatus = ProcessingStatus.IDLE,
                errorMessage = null,
                finalSize = null,
                originalSize = "N/A",
                videoTitle = ""
            )
            return
        }

        val name = getFileName(uri)
        _uiState.value = _uiState.value.copy(
            selectedVideoUri = uri,
            selectedVideoName = name,
            videoTitle = name?.substringBeforeLast('.') ?: "Untitled Video",
            // processingStatus will be updated by the launched coroutine
            errorMessage = null,
            finalSize = null
        )
        viewModelScope.launch(Dispatchers.IO) {
            val originalFileSizeBytes = getFileSize(uri)
            _uiState.value = _uiState.value.copy(
                originalSize = formatFileSize(originalFileSizeBytes),
                processingStatus = ProcessingStatus.IDLE // Ensure it's IDLE after selection
            )
        }
    }

    fun setVideoTitle(title: String) {
        _uiState.value = _uiState.value.copy(videoTitle = title)
    }

    private fun getFileName(uri: Uri): String? {
        var name: String? = null
        if (uri.scheme == "content") {
            application.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        name = cursor.getString(nameIndex)
                    }
                }
            }
        }
        if (name == null) {
            name = uri.pathSegments.lastOrNull()
        }
        return name
    }

    fun startVideoProcessing() {
        val currentUri = _uiState.value.selectedVideoUri ?: return
        val title = _uiState.value.videoTitle.ifBlank { "Untitled Video" }

        if (videosDataHelper == null && databaseName.isNotBlank()) {
             videosDataHelper = VideosScriptsDataHelper(application, databaseName)
        } else if (videosDataHelper == null) {
            _uiState.value = _uiState.value.copy(processingStatus = ProcessingStatus.ERROR, errorMessage = "Database not initialized.")
            return
        }

        _uiState.value = _uiState.value.copy(processingStatus = ProcessingStatus.STORING, errorMessage = null) // Changed from COMPRESSING
        viewModelScope.launch(Dispatchers.IO) {
            storeVideo(currentUri, title) // Removed tempCompressedFile and isOriginal arguments
        }
    }

    private suspend fun storeVideo(uri: Uri, title: String) {
        withContext(Dispatchers.IO) {
            try {
                val contentResolver = application.contentResolver
                val inputStream = contentResolver.openInputStream(uri) ?: throw Exception("Failed to open input stream for URI: $uri")
                val videoData = inputStream.readBytes()
                inputStream.close()

                val originalSizeBytes = videoData.size.toLong()

                // Get metadata using FFprobe for the file being stored
//                val mediaInfoSession = FFprobeKit.getMediaInformation(uri.toString()) // Use uri of the file to be stored
//                val durationMs = mediaInfoSession.mediaInformation?.duration?.toLongOrNull()
//                val videoStreamInfo = mediaInfoSession.mediaInformation?.streams?.firstOrNull { it.type == "video" }
//                val width = videoStreamInfo?.width?.toInt()
//                val height = videoStreamInfo?.height?.toInt()
//                val mimeType = videoStreamInfo?.codec // More specific mime type can be derived

//                val metadata = VideoMetadata(
//                    id = UUID.randomUUID().toString(),
//                    title = title,
//                    originalFileName = _uiState.value.selectedVideoName ?: "Unknown",
//                    durationMs = durationMs,
//                    resolutionWidth = width,
//                    resolutionHeight = height,
//                    originalSizeBytes = originalSizeBytes, 
//                    compressedSizeBytes = null, // Field is no longer relevant or should be updated in VideoMetadata
//                    mimeType = mimeType ?: contentResolver.getType(uri), // Fallback to content resolver type
//                    dateAdded = System.currentTimeMillis()
//                )

                //videosDataHelper?.insertVideo(metadata, videoData)

                _uiState.value = _uiState.value.copy(
                    processingStatus = ProcessingStatus.SUCCESS,
                    finalSize = formatFileSize(videoData.size.toLong()),
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    processingStatus = ProcessingStatus.ERROR,
                    errorMessage = "Failed to store video: ${e.localizedMessage}"
                )
            }
        }
    }

    private fun getFileSize(uri: Uri): Long {
        return try {
            application.contentResolver.openFileDescriptor(uri, "r")?.use {
                it.statSize
            } ?: 0L
        } catch (e: Exception) {
            // Fallback for URIs that don't support statSize directly (e.g. some http URIs)
            // This is a rough fallback, actual size might differ.
            try {
                application.contentResolver.openInputStream(uri)?.use {
                    val data = it.readBytes()
                    data.size.toLong()
                } ?: 0L
            } catch (ioe: Exception) {
                0L
            }

        }
    }

    private fun formatFileSize(sizeBytes: Long): String {
        if (sizeBytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(sizeBytes.toDouble()) / Math.log10(1024.0)).toInt()
        if (digitGroups >= units.size) return "Large File"
        return String.format("%.1f %s", sizeBytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
    }
}
