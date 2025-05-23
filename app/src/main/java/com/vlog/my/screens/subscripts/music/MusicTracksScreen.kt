package com.vlog.my.screens.subscripts.music

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.vlog.my.data.scripts.music.MusicItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicTracksScreen(
    navController: NavController,
    subScriptId: String,
    musicDatabaseName: String
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModel: MusicTracksViewModel = viewModel(
        factory = MusicTracksViewModelFactory(application, musicDatabaseName)
    )
    var showEditOrAddDialog by remember { mutableStateOf(false) }
    var editingTrack by remember { mutableStateOf<MusicItem?>(null) }
    val tracks by viewModel.musicTracks.collectAsState()
    val currentlyPlayingTrackId by viewModel.currentlyPlayingTrackId.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Manage Music Tracks") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "SubScript ID: $subScriptId", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Music Database: $musicDatabaseName", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { editingTrack = null; showEditOrAddDialog = true }) {
                Text("Add Track")
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (showEditOrAddDialog) {
                AddMusicTrackDialog(
                    onDismiss = { showEditOrAddDialog = false; editingTrack = null },
                    existingTrack = editingTrack,
                    onSaveTrack = { title, artist, album, filePathFromDialog, urlFromDialog, musicDataFromDialog ->
                        if (editingTrack == null) { // Adding new track
                            viewModel.addMusicTrack(title, artist, album, filePathFromDialog, urlFromDialog, musicDataFromDialog)
                        } else { // Editing existing track
                            val updatedTrack = editingTrack!!.copy(
                                title = title,
                                artist = artist,
                                album = album,
                                filePath = filePathFromDialog,
                                url = urlFromDialog,
                                musicData = musicDataFromDialog
                            )
                            viewModel.updateMusicTrack(updatedTrack)
                        }
                        showEditOrAddDialog = false
                        editingTrack = null
                    }
                )
            }

            if (tracks.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("No music tracks found.", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(tracks) { track ->
                        val isPlaying = track.id == currentlyPlayingTrackId
                        MusicTrackRow(
                            track = track,
                            isPlaying = isPlaying,
                            onDeleteClick = { viewModel.deleteMusicTrack(track.id) },
                            onEditClick = { editingTrack = track; showEditOrAddDialog = true },
                            onPlayClick = {
                                if (isPlaying) {
                                    // If already "playing" (prepared), this could be a resume if paused,
                                    // or re-prepare. For now, let's assume it means play the current track.
                                    viewModel.playTrack(track.id, application)
                                } else {
                                    viewModel.prepareTrackForPlayback(track, application)
                                }
                            },
                            onPauseClick = { viewModel.pausePlayback(application) },
                            onStopClick = { viewModel.stopPlayback(application) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun MusicTrackRow(
    track: MusicItem,
    isPlaying: Boolean,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit,
    onPlayClick: () -> Unit,
    onPauseClick: () -> Unit,
    onStopClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = track.title, style = MaterialTheme.typography.titleMedium)
                track.artist?.let {
                    Text(text = "Artist: $it", style = MaterialTheme.typography.bodyMedium)
                }
                track.album?.let {
                    Text(text = "Album: $it", style = MaterialTheme.typography.bodySmall)
                }
                track.filePath?.let {
                    Text(text = "File: $it", style = MaterialTheme.typography.bodySmall)
                }
                track.url?.let {
                    Text(text = "URL: $it", style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(modifier = Modifier.width(4.dp))

            if (isPlaying) {
                IconButton(onClick = onPauseClick) {
                    Icon(Icons.Filled.Pause, contentDescription = "Pause Track")
                }
                IconButton(onClick = onStopClick) {
                    Icon(Icons.Filled.Stop, contentDescription = "Stop Track")
                }
                Icon(Icons.Filled.GraphicEq, contentDescription = "Now Playing") // Indicator
            } else {
                IconButton(onClick = onPlayClick) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = "Play Track")
                }
            }

            Spacer(modifier = Modifier.width(4.dp))
            IconButton(onClick = onEditClick) {
                Icon(Icons.Filled.Edit, contentDescription = "Edit Track")
            }
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete Track")
            }
        }
    }
}
