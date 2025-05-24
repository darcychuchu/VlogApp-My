package com.vlog.my.screens.videos

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vlog.my.data.scripts.videos.VideoMetadata
import com.vlog.my.data.scripts.videos.VideosScriptsDataHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class VideoListUiState(
    val videos: List<VideoMetadata> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class VideoListViewModel(
    private val application: Application,
    private val databaseName: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(VideoListUiState())
    val uiState: StateFlow<VideoListUiState> = _uiState.asStateFlow()

    private var videosDataHelper: VideosScriptsDataHelper? = null

    init {
        if (databaseName.isNotBlank()) {
            videosDataHelper = VideosScriptsDataHelper(application, databaseName)
            loadVideos()
        } else {
            _uiState.value = VideoListUiState(errorMessage = "Database name not provided.")
        }
    }

    fun loadVideos() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val videos = videosDataHelper?.getAllVideoMetadata() ?: emptyList()
                _uiState.value = _uiState.value.copy(videos = videos, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to load videos: ${e.localizedMessage}",
                    isLoading = false
                )
            }
        }
    }

    // In a real app, you might want to clear the helper if the ViewModel is cleared
    // and the databaseName could change, but for this structure, it's initialized once.
}
