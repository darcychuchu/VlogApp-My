package com.vlog.my.screens.subscripts.videos

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditVideoScriptScreen(
    navController: NavController,
    scriptId: String?,
    viewModel: EditVideoScriptViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val localFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        uris.forEach { uri ->
            val fileName = uri.pathSegments.lastOrNull() ?: "unknown_video"
            viewModel.addSelectedFile(uri, fileName)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Edit Video Script") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = viewModel.videoScriptTitle.value,
                onValueChange = { viewModel.videoScriptTitle.value = it },
                label = { Text("Video Script Title") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (viewModel.databaseName.value.isNotEmpty()){
                Text("Database Name: ${viewModel.databaseName.value}", style = MaterialTheme.typography.labelSmall)
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text("Local Video Files", style = MaterialTheme.typography.titleMedium)
            Button(onClick = { localFilePickerLauncher.launch("video/*") }) { // For video files
                Icon(Icons.Default.FileOpen, contentDescription = "Select Video Files")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Video Files")
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (viewModel.localVideoFiles.isNotEmpty()) {
                LazyColumn(modifier = Modifier.heightIn(max = 150.dp)) {
                    items(viewModel.localVideoFiles, key = { it.id }) { fileItem ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(fileItem.name, modifier = Modifier.weight(1f))
                            IconButton(onClick = { viewModel.removeSelectedFile(fileItem) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove File")
                            }
                        }
                    }
                }
            } else {
                Text("No local files.")
            }
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()){
               Text("Network Video URLs", style = MaterialTheme.typography.titleMedium)
               IconButton(onClick = { viewModel.addNetworkVideoField() }) {
                   Icon(Icons.Default.Add, contentDescription = "Add Network URL")
               }
            }
            if (viewModel.networkVideoUrls.isNotEmpty()){
               LazyColumn(modifier = Modifier.heightIn(max = 150.dp)) {
                   items(viewModel.networkVideoUrls, key = { it.id }) { urlItem ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = urlItem.url,
                                onValueChange = { viewModel.updateNetworkVideoUrl(urlItem, it) },
                                label = { Text("Network URL") },
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { viewModel.removeNetworkVideoField(urlItem) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove URL")
                            }
                        }
                    }
               }
            } else {
                Text("No network URLs.")
            }

            Spacer(modifier = Modifier.weight(1f)) 

            Button(
                onClick = {
                    viewModel.updateVideoScript {
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Update Video Script")
            }
        }
    }
}
