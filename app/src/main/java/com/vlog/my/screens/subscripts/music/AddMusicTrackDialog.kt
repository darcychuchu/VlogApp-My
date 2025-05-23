package com.vlog.my.screens.subscripts.music

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.vlog.my.data.scripts.music.MusicItem // Added import
import kotlinx.coroutines.launch
import java.io.IOException
import androidx.compose.runtime.LaunchedEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMusicTrackDialog(
    onDismiss: () -> Unit,
    onSaveTrack: (title: String, artist: String?, album: String?, filePath: String?, url: String?, musicData: ByteArray?) -> Unit,
    existingTrack: MusicItem? = null
) {
    var title by remember { mutableStateOf(existingTrack?.title ?: "") }
    var artist by remember { mutableStateOf(existingTrack?.artist ?: "") }
    var album by remember { mutableStateOf(existingTrack?.album ?: "") }
    var networkUrl by remember { mutableStateOf(existingTrack?.url ?: "") }
    var titleError by remember { mutableStateOf(false) }

    var selectedFileUri by remember { mutableStateOf<Uri?>(null) } // Not directly pre-filled from existingTrack.filePath String
    var selectedFileName by remember { mutableStateOf(existingTrack?.filePath) }
    var musicDataState by remember { mutableStateOf(existingTrack?.musicData) }
    var isReadingFile by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(existingTrack) {
        existingTrack?.let {
            title = it.title
            artist = it.artist ?: ""
            album = it.album ?: ""
            networkUrl = it.url ?: ""
            selectedFileName = it.filePath
            musicDataState = it.musicData
            // If existing track has musicData, it implies it was a local file.
            // If it also has a URL, the URL should not be active if local data is present.
            if (it.musicData != null || it.filePath != null) {
                networkUrl = ""
            }
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedFileUri = it
            selectedFileName = try {
                context.contentResolver.query(it, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)
                    cursor.moveToFirst()
                    cursor.getString(nameIndex)
                }
            } catch (e: Exception) {
                Log.e("AddMusicTrackDialog", "Error getting file name", e)
                "Selected File"
            }

            // Clear previous music data and URL if a new file is selected
            musicDataState = null
            networkUrl = "" // Clear network URL

            coroutineScope.launch {
                isReadingFile = true
                try {
                    context.contentResolver.openInputStream(it)?.use { inputStream ->
                        musicDataState = inputStream.readBytes()
                    }
                    // If successful, networkUrl is already cleared
                } catch (e: IOException) {
                    Log.e("AddMusicTrackDialog", "Error reading file", e)
                    selectedFileName = null // Clear selection on error
                    musicDataState = null
                    // TODO: Show a toast or error message to the user
                } finally {
                    isReadingFile = false
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existingTrack == null) "Add New Music Track" else "Edit Music Track") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; titleError = false },
                    label = { Text("Title *") },
                    isError = titleError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (titleError) {
                    Text("Title cannot be empty", color = MaterialTheme.colorScheme.error)
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = artist,
                    onValueChange = { artist = it },
                    label = { Text("Artist") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = album,
                    onValueChange = { album = it },
                    label = { Text("Album") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = networkUrl,
                    onValueChange = { networkUrl = it; if (it.isNotBlank()) musicDataState = null; selectedFileName = null },
                    label = { Text("Network URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { filePickerLauncher.launch("audio/*") }) {
                    Text("Select Local File")
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (isReadingFile) {
                    Row {
                        CircularProgressIndicator(modifier = Modifier.height(20.dp))
                        Spacer(modifier = Modifier.fillMaxWidth(0.1f))
                        Text("Reading file...")
                    }
                } else if (selectedFileName != null) {
                    Text("Selected: $selectedFileName")
                } else if (musicDataState != null) {
                    Text("File selected, data loaded")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isBlank()) {
                        titleError = true
                    } else {
                        val finalUrl = if (musicDataState != null) null else networkUrl.takeIf { it.isNotBlank() }
                        onSaveTrack(
                            title,
                            artist.takeIf { it.isNotBlank() },
                            album.takeIf { it.isNotBlank() },
                            selectedFileName,
                            finalUrl,
                            musicDataState
                        )
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
