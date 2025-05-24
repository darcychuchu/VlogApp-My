package com.vlog.my.screens.subscripts.music

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMusicScreen(
    navController: NavController,
    viewModel: AddMusicViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val localFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        uris.forEach { uri ->
            // Get file name from URI
            val fileName = uri.pathSegments.lastOrNull() ?: "unknown_file"
            viewModel.addSelectedFile(uri, fileName)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Add New Music Script") },
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
                value = viewModel.musicTitle.value,
                onValueChange = { viewModel.musicTitle.value = it },
                label = { Text("Music Script Title") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Local Music Files Section
            Text("Local Music Files", style = MaterialTheme.typography.titleMedium)
            Button(onClick = { localFilePickerLauncher.launch("audio/*") }) {
                Icon(Icons.Default.FileOpen, contentDescription = "Select Audio Files")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Select Audio Files")
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (viewModel.localMusicFiles.isNotEmpty()) {
                LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) { // Limit height
                    items(viewModel.localMusicFiles, key = { it.id }) { fileItem ->
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
                Text("No local files selected.")
            }


            Spacer(modifier = Modifier.height(16.dp))

            // Network Music URLs Section
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()){
               Text("Network Music URLs", style = MaterialTheme.typography.titleMedium)
               IconButton(onClick = { viewModel.addNetworkMusicField() }) {
                   Icon(Icons.Default.Add, contentDescription = "Add Network URL")
               }
            }

            if (viewModel.networkMusicUrls.isNotEmpty()){
               LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) { // Limit height
                   items(viewModel.networkMusicUrls, key = { it.id }) { urlItem ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = urlItem.url,
                                onValueChange = { viewModel.updateNetworkMusicUrl(urlItem, it) },
                                label = { Text("Network URL") },
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { viewModel.removeNetworkMusicField(urlItem) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove URL")
                            }
                        }
                    }
               }
            } else {
                Text("No network URLs added.")
            }


            Spacer(modifier = Modifier.weight(1f)) // Push save button to bottom

            Button(
                onClick = {
                    viewModel.saveMusicScript {
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Music Script")
            }
        }
    }
}
