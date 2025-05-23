package com.vlog.my.screens.videos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Share // For Export/Backup icon
import androidx.compose.material.icons.filled.Videocam // Generic video icon
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.vlog.my.data.scripts.videos.VideoMetadata
import java.util.concurrent.TimeUnit

// Helper function to format duration from milliseconds to MM:SS or HH:MM:SS
fun formatDuration(millis: Long?): String {
    if (millis == null || millis < 0) return "N/A"
    val hours = TimeUnit.MILLISECONDS.toHours(millis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1)
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

// Helper function to format file size
fun formatSize(sizeBytes: Long?): String {
    if (sizeBytes == null || sizeBytes <= 0) return "N/A"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(sizeBytes.toDouble()) / Math.log10(1024.0)).toInt()
    if (digitGroups == 0) return "$sizeBytes B"
    if (digitGroups >= units.size) return "Large"
    return String.format("%.1f %s", sizeBytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoListScreen(
    navController: NavController,
    databaseName: String,
    scriptId: String // Assuming scriptId is the same as databaseName or part of it
) {
    val application = LocalContext.current.applicationContext as android.app.Application
    val viewModel: VideoListViewModel = viewModel(
        factory = VideoListViewModelFactory(application, databaseName)
    )
    val uiState by viewModel.uiState.collectAsState()

    var showExportDialog by remember { mutableStateOf(false) }
    val localContext = LocalContext.current // For database path

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Backup Video Script") },
            text = {
                val dbPath = localContext.getDatabasePath(databaseName).absolutePath
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("This video script's data is stored in an SQLite database file.")
                    Text("Database file name: $databaseName")
                    Text("Full path: $dbPath")
                    Text("To back up your videos, use a file manager to copy this file to a safe location (e.g., cloud storage, your computer).")
                    Text("To restore later, you will use the 'Import from backup' option when creating a new video script.")
                }
            },
            confirmButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Dismiss")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Videos in $scriptId") },
                actions = {
                    var expanded by remember { mutableStateOf(false) }
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Backup/Export Script Info") },
                            onClick = {
                                showExportDialog = true
                                expanded = false
                            },
                            leadingIcon = { Icon(Icons.Filled.Share, contentDescription = "Backup Script") }
                        )
                        // Add other menu items here if needed in the future
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                // Navigate to AddVideoScreen, passing the databaseName
                navController.navigate("add_video_screen/$databaseName")
            }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Video")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.errorMessage != null) {
                Text(
                    text = "Error: ${uiState.errorMessage}",
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                    color = MaterialTheme.colorScheme.error
                )
            } else if (uiState.videos.isEmpty()) {
                Text(
                    text = "No videos found. Tap the '+' button to add one.",
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.videos, key = { it.id }) { video ->
                        VideoListItem(
                            video = video,
                            onClick = {
                                // Navigate to VideoPlayerScreen
                                navController.navigate(Screen.VideoPlayer.createRoute(databaseName, video.id))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VideoListItem(
    video: VideoMetadata,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Videocam, // Generic video icon
                contentDescription = "Video",
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(video.title, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Duration: ${formatDuration(video.durationMs)}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "Size: ${formatSize(video.compressedSizeBytes ?: video.originalSizeBytes)}",
                    style = MaterialTheme.typography.bodySmall
                )
                video.resolutionWidth?.let { w ->
                    video.resolutionHeight?.let { h ->
                        Text("Resolution: ${w}x$h", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Icon(
                imageVector = Icons.Filled.PlayCircleOutline,
                contentDescription = "Play Video",
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.secondary
            )
        }
    }
}
