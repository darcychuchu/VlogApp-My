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
    val selectedCompressionOption: String = "Original", // Default option
    val compressionOptions: List<String> = listOf("Original", "640p", "720p", "1080p"),
    val processingStatus: ProcessingStatus = ProcessingStatus.IDLE,
    val estimatedSize: String = "N/A",
    val originalSize: String = "N/A",
    val finalSize: String? = null,
    val errorMessage: String? = null,
    val progress: Float = 0f,
    val workId: UUID? = null,
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
                estimatedSize = "N/A",
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
            processingStatus = ProcessingStatus.IDLE,
            errorMessage = null,
            finalSize = null
        )
        estimateSizeAndMetadata(uri, _uiState.value.selectedCompressionOption)
    }

    fun setVideoTitle(title: String) {
        _uiState.value = _uiState.value.copy(videoTitle = title)
    }

    fun setSelectedCompressionOption(option: String) {
        _uiState.value = _uiState.value.copy(selectedCompressionOption = option)
        _uiState.value.selectedVideoUri?.let {
            estimateSizeAndMetadata(it, option)
        }
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

    private fun estimateSizeAndMetadata(uri: Uri, compressionOption: String) {
        _uiState.value = _uiState.value.copy(processingStatus = ProcessingStatus.ESTIMATING_SIZE, estimatedSize = "Estimating...")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val originalFileSizeBytes = getFileSize(uri)
                _uiState.value = _uiState.value.copy(originalSize = formatFileSize(originalFileSizeBytes))

//                val mediaInformation = FFprobeKit.getMediaInformation(uri.toString()) // FFprobe needs a file path or content URI
//                val durationMs = mediaInformation.mediaInformation?.duration?.toLongOrNull() ?: 0L
//                val streams = mediaInformation.mediaInformation?.streams
//                val videoStream = streams?.firstOrNull { it.type == "video" }
//                val originalWidth = videoStream?.width?.toInt()
//                val originalHeight = videoStream?.height?.toInt()
//
//
//                val estimatedSizeBytes = when (compressionOption) {
//                    "Original" -> originalFileSizeBytes
//                    "640p" -> calculateRoughEstimatedSize(originalFileSizeBytes, originalWidth, originalHeight, 640, -1)
//                    "720p" -> calculateRoughEstimatedSize(originalFileSizeBytes, originalWidth, originalHeight, -1, 720)
//                    "1080p" -> calculateRoughEstimatedSize(originalFileSizeBytes, originalWidth, originalHeight, -1, 1080)
//                    else -> originalFileSizeBytes / 3 // Default fallback if calculation fails
//                }
//                _uiState.value = _uiState.value.copy(
//                    estimatedSize = formatFileSize(estimatedSizeBytes),
//                    processingStatus = ProcessingStatus.IDLE
//                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    estimatedSize = "Error estimating",
                    originalSize = formatFileSize(getFileSize(uri)), // Still show original if ffprobe fails
                    processingStatus = ProcessingStatus.IDLE,
                    errorMessage = "Could not get video metadata: ${e.localizedMessage}"
                )
            }
        }
    }
     private fun calculateRoughEstimatedSize(originalSizeBytes: Long, originalWidth: Int?, originalHeight: Int?, targetWidth: Int, targetHeight: Int): Long {
        if (originalSizeBytes <= 0 || originalWidth == null || originalHeight == null || originalWidth <= 0 || originalHeight <= 0) {
            return originalSizeBytes / 3 // Fallback if original dimensions are unknown
        }
        val originalPixels = originalWidth * originalHeight.toDouble()
        
        val actualTargetWidth = if (targetWidth > 0) targetWidth.toDouble() else (targetHeight.toDouble() / originalHeight) * originalWidth
        val actualTargetHeight = if (targetHeight > 0) targetHeight.toDouble() else (targetWidth.toDouble() / originalWidth) * originalHeight
        
        val targetPixels = actualTargetWidth * actualTargetHeight

        if (targetPixels <= 0) return originalSizeBytes / 3 // Fallback

        // Assume roughly linear relationship between pixels and size, with a quality factor (e.g., 0.7 for some compression)
        // This is a very rough heuristic.
        val qualityFactor = 0.5 
        var estimated = (originalSizeBytes * (targetPixels / originalPixels) * qualityFactor).toLong()

        // Ensure estimated size is not larger than original if we are downscaling
        if (targetPixels < originalPixels && estimated > originalSizeBytes) {
            estimated = originalSizeBytes / 2 // Adjust if estimation is off
        }
        if (estimated <=0) estimated = originalSizeBytes / 3 // ensure not zero

        return estimated
    }


    fun startVideoProcessing() {
        val currentUri = _uiState.value.selectedVideoUri ?: return
        val compressionOption = _uiState.value.selectedCompressionOption
        val title = _uiState.value.videoTitle.ifBlank { "Untitled Video" }

        if (videosDataHelper == null && databaseName.isNotBlank()) {
             videosDataHelper = VideosScriptsDataHelper(application, databaseName)
        } else if (videosDataHelper == null) {
            _uiState.value = _uiState.value.copy(processingStatus = ProcessingStatus.ERROR, errorMessage = "Database not initialized.")
            return
        }


        _uiState.value = _uiState.value.copy(processingStatus = ProcessingStatus.COMPRESSING, errorMessage = null, progress = 0f)

        if (compressionOption == "Original") {
            viewModelScope.launch(Dispatchers.IO) {
                _uiState.value = _uiState.value.copy(processingStatus = ProcessingStatus.STORING)
                storeVideo(currentUri, null, title, true)
            }
        } else {
            val outputFileName = "compressed_${UUID.randomUUID()}.mp4"
            val outputFile = File(application.cacheDir, outputFileName)

            val workRequest = OneTimeWorkRequestBuilder<VideoCompressionWorker>()
                .setInputData(
                    workDataOf(
                        VideoCompressionWorker.KEY_INPUT_URI to currentUri.toString(),
                        VideoCompressionWorker.KEY_OUTPUT_PATH to outputFile.absolutePath,
                        VideoCompressionWorker.KEY_COMPRESSION_OPTION to compressionOption
                    )
                )
                .build()

            _uiState.value = _uiState.value.copy(workId = workRequest.id)
            workManager.enqueue(workRequest)
            observeWork(workRequest.id, outputFile, title)
        }
    }

    private fun observeWork(workId: UUID, compressedFile: File, title: String) {
        workManager.getWorkInfoByIdLiveData(workId).observeForever { workInfo ->
            when (workInfo?.state) {
                WorkInfo.State.SUCCEEDED -> {
                    viewModelScope.launch(Dispatchers.IO) {
                        val outputPath = workInfo.outputData.getString(VideoCompressionWorker.KEY_OUTPUT_VIDEO_PATH)
                        if (outputPath != null) {
                            _uiState.value = _uiState.value.copy(processingStatus = ProcessingStatus.STORING)
                            storeVideo(Uri.fromFile(File(outputPath)), compressedFile, title, false)
                        } else {
                            _uiState.value = _uiState.value.copy(processingStatus = ProcessingStatus.ERROR, errorMessage = "Compressed file path missing.")
                        }
                    }
                }
                WorkInfo.State.FAILED -> {
                    val error = workInfo.outputData.getString(VideoCompressionWorker.KEY_ERROR_MESSAGE) ?: "Compression failed."
                    _uiState.value = _uiState.value.copy(processingStatus = ProcessingStatus.ERROR, errorMessage = error)
                    if (compressedFile.exists()) compressedFile.delete()
                }
                WorkInfo.State.RUNNING -> {
                     _uiState.value = _uiState.value.copy(processingStatus = ProcessingStatus.COMPRESSING)
                    // You can get progress from workInfo.progress if the worker sets it
                    // val progress = workInfo.progress.getFloat(VideoCompressionWorker.KEY_PROGRESS, 0f)
                    // _uiState.value = _uiState.value.copy(progress = progress)
                }
                WorkInfo.State.ENQUEUED -> {
                     _uiState.value = _uiState.value.copy(processingStatus = ProcessingStatus.COMPRESSING, progress = 0f)
                }
                else -> { /* Other states like BLOCKED, CANCELLED */ }
            }
        }
    }

    private suspend fun storeVideo(uri: Uri, tempCompressedFile: File?, title: String, isOriginal: Boolean) {
        withContext(Dispatchers.IO) {
            try {
                val contentResolver = application.contentResolver
                val inputStream = contentResolver.openInputStream(uri) ?: throw Exception("Failed to open input stream for URI: $uri")
                val videoData = inputStream.readBytes()
                inputStream.close()

                val originalSizeBytes = if (isOriginal) videoData.size.toLong() else getFileSize(_uiState.value.selectedVideoUri!!) // Approx if original was remote
                val compressedSizeBytes = if (isOriginal) null else videoData.size.toLong()

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
//                    originalSizeBytes = if(isOriginal) originalSizeBytes else getFileSize(_uiState.value.selectedVideoUri!!), // Size of the original selected video
//                    compressedSizeBytes = if(isOriginal) null else compressedSizeBytes, // Size of the actual videoData stored
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
            } finally {
                tempCompressedFile?.delete()
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
