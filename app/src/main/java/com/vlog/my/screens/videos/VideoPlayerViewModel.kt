package com.vlog.my.screens.videos

import android.app.Application
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.vlog.my.data.scripts.videos.VideoMetadata
import com.vlog.my.data.scripts.videos.VideosScriptsDataHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.core.net.toUri

@UnstableApi
data class VideoPlayerUiState(
    val currentPlayer: ExoPlayer? = null,
    val currentVideoMetadata: VideoMetadata? = null, // For local DB videos
    val currentVideoUrl: String? = null, // For URL-based videos
    val playlist: List<VideoMetadata> = emptyList(), // Only for local DB videos
    val currentVideoIndex: Int = -1, // Only for local DB videos
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val totalDuration: Long = 0L,
    val resizeMode: Int = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
)

@UnstableApi // For ByteArrayDataSource and potentially other Media3 APIs
class VideoPlayerViewModel(
    private val application: Application,
    private val databaseName: String?, // Nullable if playing from URL
    private val initialVideoId: String?, // Nullable if playing from URL
    private val initialVideoUrl: String?, // New parameter
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(VideoPlayerUiState())
    val uiState: StateFlow<VideoPlayerUiState> = _uiState.asStateFlow()

    private var videosDataHelper: VideosScriptsDataHelper? = null
    private var player: ExoPlayer? = null

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _uiState.value = _uiState.value.copy(isPlaying = isPlaying)
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_ENDED) {
                playNext()
            }
        }

        // We can update currentPosition and totalDuration more frequently if needed
        // using a separate coroutine that polls player.currentPosition and player.duration
    }

    init {
        initializePlayer()
        if (!databaseName.isNullOrBlank() && !initialVideoId.isNullOrBlank()) {
            videosDataHelper = VideosScriptsDataHelper(application, databaseName)
            loadPlaylistAndPlayInitialFromDb()
        } else if (!initialVideoUrl.isNullOrBlank()) {
            prepareAndPlayUrlVideo(initialVideoUrl, savedStateHandle.get<Long>("currentPosition") ?: 0L)
        } else {
            _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "No video source provided.")
        }
    }

    private fun initializePlayer() {
        player = ExoPlayer.Builder(application).build().apply {
            addListener(playerListener)
        }
        _uiState.value = _uiState.value.copy(currentPlayer = player)
    }

    private fun loadPlaylistAndPlayInitialFromDb() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val allVideos = videosDataHelper?.getAllVideoMetadata() ?: emptyList()
                _uiState.value = _uiState.value.copy(playlist = allVideos)

                val restoredVideoId = savedStateHandle.get<String>("currentVideoId") ?: initialVideoId
                val restoredPosition = savedStateHandle.get<Long>("currentPosition") ?: 0L

                val videoToPlay = allVideos.find { it.id == restoredVideoId } 
                    ?: allVideos.find { it.id == initialVideoId }

                if (videoToPlay != null) {
                    val index = allVideos.indexOf(videoToPlay)
                    prepareAndPlayLocalVideo(videoToPlay, index, restoredPosition)
                } else if (allVideos.isNotEmpty() && initialVideoId.isNullOrBlank() && initialVideoUrl.isNullOrBlank()) {
                    // Fallback to first video if no specific ID/URL was given and playlist is not empty
                    prepareAndPlayLocalVideo(allVideos[0], 0, 0L)
                } else if (initialVideoId.isNullOrBlank() && initialVideoUrl.isNullOrBlank()) {
                     _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "No videos found in this script.")
                } else if (videoToPlay == null && !initialVideoId.isNullOrBlank()){
                    // Specific video ID was given but not found
                     _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Video with ID '$initialVideoId' not found in script '$databaseName'.")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Error loading playlist: ${e.localizedMessage}")
            }
        }
    }

    private suspend fun prepareAndPlayLocalVideo(videoMetadata: VideoMetadata, index: Int, startPosition: Long = 0L) {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            currentVideoMetadata = videoMetadata,
            currentVideoUrl = null, // Clear any URL
            currentVideoIndex = index,
            errorMessage = null
        )
        withContext(Dispatchers.IO) {
            val videoData = videosDataHelper?.getVideoData(videoMetadata.id)
            if (videoData != null) {
                val dataSourceFactory = ByteArrayDataSource.Factory(videoData)
                val mediaItem = MediaItem.Builder()
                    .setUri("bytes://${videoMetadata.id}".toUri())
                    .setMimeType(videoMetadata.mimeType ?: "video/mp4")
                    //.setDataSourceFactory(dataSourceFactory)
                    .build()
                
                withContext(Dispatchers.Main) {
                    player?.setMediaItem(mediaItem)
                    player?.prepare()
                    player?.seekTo(startPosition)
                    player?.playWhenReady = true
                    _uiState.value = _uiState.value.copy(isLoading = false, isPlaying = player?.isPlaying ?: false)
                    startPositionUpdater()
                }
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Video data not found for ${videoMetadata.title}")
            }
        }
    }

    private fun prepareAndPlayUrlVideo(videoUrl: String, startPosition: Long = 0L) {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            currentVideoUrl = videoUrl,
            currentVideoMetadata = null, // Clear local metadata
            playlist = emptyList(), // No playlist for single URL
            currentVideoIndex = 0, // Single item at index 0
            errorMessage = null
        )
        viewModelScope.launch(Dispatchers.Main) { // Player operations on Main
            try {
                val mediaItem = MediaItem.fromUri(videoUrl)
                player?.setMediaItem(mediaItem)
                player?.prepare()
                player?.seekTo(startPosition)
                player?.playWhenReady = true
                _uiState.value = _uiState.value.copy(isLoading = false, isPlaying = player?.isPlaying ?: false)
                startPositionUpdater()
            } catch (e: Exception) {
                 _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Error playing video from URL: ${e.localizedMessage}")
            }
        }
    }
    
    private fun startPositionUpdater() {
        viewModelScope.launch {
            while (player?.isPlaying == true || _uiState.value.isLoading) { // Keep updating if playing or loading
                if (player != null && player!!.isCommandAvailable(Player.COMMAND_GET_CURRENT_MEDIA_ITEM)) {
                     if (player!!.playbackState != Player.STATE_IDLE && player!!.playbackState != Player.STATE_ENDED) {
                        _uiState.value = _uiState.value.copy(
                            currentPosition = player!!.currentPosition.coerceAtLeast(0L),
                            totalDuration = player!!.duration.coerceAtLeast(0L)
                        )
                    }
                }
                kotlinx.coroutines.delay(500) // Update interval
            }
        }
    }


    fun playNext() {
        val currentIndex = _uiState.value.currentVideoIndex
        val playlist = _uiState.value.playlist
        // Only play next/prev if we are in DB playlist mode
        if (playlist.isNotEmpty() && !_uiState.value.currentVideoUrl.isNullOrBlank().not()) {
            val nextIndex = (currentIndex + 1) % playlist.size
            viewModelScope.launch {
                prepareAndPlayLocalVideo(playlist[nextIndex], nextIndex)
            }
        }
        // If playing from URL, playNext does nothing or could seek to end/restart. For now, it does nothing.
    }

    fun playPrevious() {
        val currentIndex = _uiState.value.currentVideoIndex
        val playlist = _uiState.value.playlist
        // Only play next/prev if we are in DB playlist mode
        if (playlist.isNotEmpty() && !_uiState.value.currentVideoUrl.isNullOrBlank().not()) {
            val prevIndex = if (currentIndex - 1 < 0) playlist.size - 1 else currentIndex - 1
            viewModelScope.launch {
                prepareAndPlayLocalVideo(playlist[prevIndex], prevIndex)
            }
        }
        // If playing from URL, playPrevious does nothing or could seek to start. For now, it does nothing.
    }

    fun toggleResizeMode() {
        val currentMode = _uiState.value.resizeMode
        val newMode = if (currentMode == androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT) {
            androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
        } else {
            androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
        }
        _uiState.value = _uiState.value.copy(resizeMode = newMode)
    }

    fun pausePlayer() {
        player?.pause()
    }

    fun resumePlayer() {
        player?.play()
    }
    
    fun seekTo(positionMs: Long) {
        player?.seekTo(positionMs)
        _uiState.value = _uiState.value.copy(currentPosition = positionMs)
    }


    override fun onCleared() {
        super.onCleared()
        // Save current state before releasing
        if (_uiState.value.currentVideoUrl != null) {
            // Could save URL if needed, but less common to restore URL state this way
        } else {
            _uiState.value.currentVideoMetadata?.id?.let {
                savedStateHandle["currentVideoId"] = it
            }
        }
        savedStateHandle["currentPosition"] = player?.currentPosition ?: 0L

        player?.removeListener(playerListener)
        player?.release()
        player = null
        _uiState.value = _uiState.value.copy(currentPlayer = null)
    }
}
