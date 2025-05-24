package com.vlog.my.screens.videos

import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.ActivityInfo
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.ZoomOutMap
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.vlog.my.data.scripts.videos.VideoMetadata
import com.vlog.my.utils.LockScreenOrientation // Assuming this utility exists from problem context


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerScreen(
    navController: NavController,
    databaseName: String,
    initialVideoId: String,
    initialVideoUrl: String?
) {
    val application = LocalContext.current.applicationContext as Application
    val viewModel: VideoPlayerViewModel = viewModel(
        factory = VideoPlayerViewModelFactory(application, databaseName, initialVideoId, initialVideoUrl)
    )
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var isFullScreen by remember { mutableStateOf(false) } // Manage fullscreen state

    // Lock to landscape if in fullscreen, otherwise allow sensor
    LockScreenOrientation(if (isFullScreen) ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
    // Attempt to hide/show system UI based on fullscreen state
    //SetFullScreen(window = (context as? android.app.Activity)?.window, fullscreen = isFullScreen)


    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> viewModel.pausePlayer()
                Lifecycle.Event.ON_RESUME -> viewModel.resumePlayer()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            // Player is released in ViewModel's onCleared
        }
    }
    
    BackHandler(enabled = isFullScreen) {
        isFullScreen = false
    }


    Scaffold(
        topBar = {
            if (!isFullScreen) {
                TopAppBar(
                    title = { Text(uiState.currentVideoMetadata?.title ?: "Video Player") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
//                    actions = {
//                        IconButton(onClick = { viewModel.toggleResizeMode() }) {
//                            Icon(
//                                if (uiState.resizeMode == AspectRatioFrameLayout.RESIZE_MODE_FIT) Icons.Filled.FitScreen else Icons.Filled.ZoomOutMap,
//                                contentDescription = "Toggle Resize Mode"
//                            )
//                        }
//                        IconButton(onClick = { isFullScreen = !isFullScreen }) {
//                            Icon(
//                                if (isFullScreen) Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen,
//                                contentDescription = "Toggle Fullscreen"
//                            )
//                        }
//                    }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(if (isFullScreen) PaddingValues(0.dp) else paddingValues)
                .fillMaxSize()
                .background(Color.Black),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else if (uiState.errorMessage != null) {
                Text(uiState.errorMessage!!, color = Color.White)
            } else if (uiState.currentPlayer != null) {
//                AndroidView(
//                    factory = { ctx ->
//                        PlayerView(ctx).apply {
//                            player = uiState.currentPlayer
//                            useController = true // Enable default controls
//                            resizeMode = uiState.resizeMode
//                            layoutParams = FrameLayout.LayoutParams(
//                                ViewGroup.LayoutParams.MATCH_PARENT,
//                                ViewGroup.LayoutParams.MATCH_PARENT
//                            )
//                        }
//                    },
//                    update = { playerView ->
//                        playerView.player = uiState.currentPlayer
//                        playerView.resizeMode = uiState.resizeMode
//                    },
//                    modifier = Modifier.weight(1f).fillMaxWidth()
//                )

                // Custom controls if needed, or for additional functionality like playlist navigation
                if (!isFullScreen) { // Show these controls only when not in fullscreen
                    VideoControls(
                        isPlaying = uiState.isPlaying,
                        onPlayPause = { if (uiState.isPlaying) viewModel.pausePlayer() else viewModel.resumePlayer() },
                        onNext = { viewModel.playNext() },
                        onPrevious = { viewModel.playPrevious() },
                        currentPosition = uiState.currentPosition,
                        totalDuration = uiState.totalDuration,
                        onSeek = { position -> viewModel.seekTo(position) },
                        playlist = uiState.playlist,
                        currentVideoIndex = uiState.currentVideoIndex
                    )
                }
            } else {
                Text("Player not available.", color = Color.White)
            }
        }
    }
}

@Composable
fun VideoControls(
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    currentPosition: Long,
    totalDuration: Long,
    onSeek: (Long) -> Unit,
    playlist: List<VideoMetadata>,
    currentVideoIndex: Int
) {
    Column(modifier = Modifier.padding(8.dp).fillMaxWidth()) {
        Slider(
            value = currentPosition.toFloat(),
            onValueChange = { onSeek(it.toLong()) },
            valueRange = 0f..(totalDuration.toFloat().coerceAtLeast(0f)),
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(formatDuration(currentPosition), style = MaterialTheme.typography.bodySmall.copy(color = Color.White))
            Text(formatDuration(totalDuration), style = MaterialTheme.typography.bodySmall.copy(color = Color.White))
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrevious, enabled = playlist.isNotEmpty() && currentVideoIndex > 0) {
                Icon(Icons.Filled.SkipPrevious, contentDescription = "Previous", tint = Color.White)
            }
            IconButton(onClick = onPlayPause) {
                Icon(
                    if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
            IconButton(onClick = onNext, enabled = playlist.isNotEmpty() && currentVideoIndex < playlist.size -1) {
                Icon(Icons.Filled.SkipNext, contentDescription = "Next", tint = Color.White)
            }
        }
    }
}

// Helper function to format duration from milliseconds to MM:SS or HH:MM:SS
@SuppressLint("DefaultLocale")
private fun formatDuration(millis: Long): String {
    val hours = java.util.concurrent.TimeUnit.MILLISECONDS.toHours(millis)
    val minutes = java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(millis) % java.util.concurrent.TimeUnit.HOURS.toMinutes(1)
    val seconds = java.util.concurrent.TimeUnit.MILLISECONDS.toSeconds(millis) % java.util.concurrent.TimeUnit.MINUTES.toSeconds(1)
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
