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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMusicScreen(
    navController: NavController,
    scriptId: String?, // Passed from navigation
    viewModel: EditMusicViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val localFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        uris.forEach { uri ->
            val fileName = uri.pathSegments.lastOrNull() ?: "unknown_file"
            viewModel.addSelectedFile(uri, fileName)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Edit Music Script") },
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
            Spacer(modifier = Modifier.height(8.dp))
            if (viewModel.databaseName.value.isNotEmpty()){ // Display DB name, but don't allow editing
                Text("Database Name: ${viewModel.databaseName.value}", style = MaterialTheme.typography.labelSmall)
            }
            Spacer(modifier = Modifier.height(16.dp))


            Text("Local Music Files", style = MaterialTheme.typography.titleMedium)
            Button(onClick = { localFilePickerLauncher.launch("audio/*") }) {
                Icon(Icons.Default.FileOpen, contentDescription = "Select Audio Files")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Audio Files")
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (viewModel.localMusicFiles.isNotEmpty()) {
                LazyColumn(modifier = Modifier.heightIn(max = 150.dp)) { // Adjusted height
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
                Text("No local files.")
            }
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()){
               Text("Network Music URLs", style = MaterialTheme.typography.titleMedium)
               IconButton(onClick = { viewModel.addNetworkMusicField() }) {
                   Icon(Icons.Default.Add, contentDescription = "Add Network URL")
               }
            }
            if (viewModel.networkMusicUrls.isNotEmpty()){
               LazyColumn(modifier = Modifier.heightIn(max = 150.dp)) { // Adjusted height
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
                Text("No network URLs.")
            }

            Spacer(modifier = Modifier.weight(1f)) 

            Button(
                onClick = {
                    viewModel.updateMusicScript {
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Update Music Script")
            }
        }
    }
}
