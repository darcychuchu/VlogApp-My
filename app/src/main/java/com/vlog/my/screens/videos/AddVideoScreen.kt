package com.vlog.my.screens.videos

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.LinearProgressIndicator
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVideoScreen(
    databaseName: String, // This should be passed when navigating to this screen
    // onNavigateUp: () -> Unit, // For later navigation
    // onUploadSuccess: () -> Unit // Callback for successful upload
) {
    val application = LocalContext.current.applicationContext as android.app.Application
    val viewModel: AddVideoViewModel = viewModel(
        factory = AddVideoViewModelFactory(application, databaseName)
    )
    val uiState by viewModel.uiState.collectAsState()

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.setSelectedVideo(uri)
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Upload New Video") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { videoPickerLauncher.launch("video/*") },
                enabled = uiState.processingStatus == ProcessingStatus.IDLE || uiState.processingStatus == ProcessingStatus.ESTIMATING_SIZE || uiState.processingStatus == ProcessingStatus.ERROR
            ) {
                Icon(Icons.Filled.FolderOpen, contentDescription = "Select Video", modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Select Video File")
            }

            uiState.selectedVideoName?.let { name ->
                Text("Selected file: $name (Original: ${uiState.originalSize})")
            } ?: Text("No video selected.")

            OutlinedTextField(
                value = uiState.videoTitle,
                onValueChange = { viewModel.setVideoTitle(it) },
                label = { Text("Video Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = uiState.selectedVideoUri != null && (uiState.processingStatus == ProcessingStatus.IDLE || uiState.processingStatus == ProcessingStatus.ESTIMATING_SIZE)
            )


            Text("Compression Options:", style = MaterialTheme.typography.titleMedium)

            var expanded by remember { mutableStateOf(false) }

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = {
                    if (uiState.processingStatus == ProcessingStatus.IDLE || uiState.processingStatus == ProcessingStatus.ESTIMATING_SIZE) {
                        expanded = !expanded
                    }
                }
            ) {
                OutlinedTextField(
                    value = uiState.selectedCompressionOption,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Resolution") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                     enabled = uiState.selectedVideoUri != null && (uiState.processingStatus == ProcessingStatus.IDLE || uiState.processingStatus == ProcessingStatus.ESTIMATING_SIZE)
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    uiState.compressionOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                viewModel.setSelectedCompressionOption(option)
                                expanded = false
                            }
                        )
                    }
                }
            }

            Text("Estimated compressed size: ${uiState.estimatedSize}")
            uiState.finalSize?.let {
                Text("Final size: $it", style = MaterialTheme.typography.labelMedium)
            }


            if (uiState.processingStatus == ProcessingStatus.COMPRESSING ||
                uiState.processingStatus == ProcessingStatus.STORING ||
                uiState.processingStatus == ProcessingStatus.ESTIMATING_SIZE) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Text(
                        when(uiState.processingStatus) {
                            ProcessingStatus.ESTIMATING_SIZE -> "Estimating size..."
                            ProcessingStatus.COMPRESSING -> "Compressing video..."
                            ProcessingStatus.STORING -> "Saving video..."
                            else -> ""
                        }
                    )
                }
            }

            uiState.errorMessage?.let { error ->
                Text("Error: $error", color = MaterialTheme.colorScheme.error)
            }

            if (uiState.processingStatus == ProcessingStatus.SUCCESS) {
                Text("Video processed and saved successfully!", color = MaterialTheme.colorScheme.primary)
                Button(onClick = {
                    viewModel.setSelectedVideo(null) // Reset for next upload
                    // onUploadSuccess() // Potentially navigate away or clear screen
                }) {
                    Text("Add Another Video")
                }
            } else {
                Button(
                    onClick = { viewModel.startVideoProcessing() },
                    enabled = uiState.selectedVideoUri != null &&
                              (uiState.processingStatus == ProcessingStatus.IDLE || uiState.processingStatus == ProcessingStatus.ERROR) &&
                              uiState.videoTitle.isNotBlank()
                ) {
                    Text("Upload and Process Video")
                }
            }
        }
    }
}
