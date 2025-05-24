package com.vlog.my.screens.subscripts.ebooks

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
fun AddEbookScreen(
    navController: NavController,
    viewModel: AddEbookViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val localFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        uris.forEach { uri ->
            val fileName = uri.pathSegments.lastOrNull() ?: "unknown_ebook_file.txt"
            // Add more robust .txt extension checking if needed, though mime type helps.
            viewModel.addSelectedFile(uri, fileName)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Add New eBook Script") },
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
                value = viewModel.ebookScriptTitle.value,
                onValueChange = { viewModel.ebookScriptTitle.value = it },
                label = { Text("eBook Script Title") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text("Local eBook Files (.txt only)", style = MaterialTheme.typography.titleMedium)
            Button(onClick = { localFilePickerLauncher.launch("text/plain") }) { // Changed to text/plain
                Icon(Icons.Default.FileOpen, contentDescription = "Select .txt Files")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Select .txt Files")
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (viewModel.localEbookFiles.isNotEmpty()) {
                LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                    items(viewModel.localEbookFiles, key = { it.id }) { fileItem ->
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
                Text("No local .txt files selected.")
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()){
               Text("Network eBook URLs", style = MaterialTheme.typography.titleMedium)
               IconButton(onClick = { viewModel.addNetworkEbookField() }) {
                   Icon(Icons.Default.Add, contentDescription = "Add Network URL")
               }
            }
            if (viewModel.networkEbookUrls.isNotEmpty()){
               LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                   items(viewModel.networkEbookUrls, key = { it.id }) { urlItem ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = urlItem.url,
                                onValueChange = { viewModel.updateNetworkEbookUrl(urlItem, it) },
                                label = { Text("Network URL") },
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { viewModel.removeNetworkEbookField(urlItem) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove URL")
                            }
                        }
                    }
               }
            } else {
                Text("No network URLs added.")
            }

            Spacer(modifier = Modifier.weight(1f)) 

            Button(
                onClick = {
                    viewModel.saveEbookScript {
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save eBook Script")
            }
        }
    }
}
