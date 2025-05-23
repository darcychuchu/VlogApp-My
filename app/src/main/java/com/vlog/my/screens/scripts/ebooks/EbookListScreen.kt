package com.vlog.my.screens.scripts.ebooks

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.vlog.my.data.scripts.SubScriptsDataHelper
import com.vlog.my.data.scripts.ebooks.Ebook
import com.vlog.my.data.scripts.ebooks.EbookImporter
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.launch
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.webkit.MimeTypeMap
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EbookListScreen(
    navController: NavController,
    subScriptId: String
) {
    val applicationContext = LocalContext.current.applicationContext as Application
    val subScriptsDataHelper = remember { SubScriptsDataHelper(applicationContext) }
    val ebookImporter = remember { EbookImporter(applicationContext) }
    val coroutineScope = rememberCoroutineScope()

    val listViewModel: EbookListViewModel = viewModel(
        factory = EbookListViewModelFactory(applicationContext, subScriptsDataHelper)
    )

    val settingsViewModel: EbookSettingsViewModel = viewModel(
        factory = EbookSettingsViewModelFactory(applicationContext, subScriptsDataHelper)
    )

    var showImportDialog by remember { mutableStateOf(false) }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var ebookTitleInput by remember { mutableStateOf("") }
    var ebookAuthorInput by remember { mutableStateOf("") }

    val databaseName = "ebooks_for_sub_$subScriptId.db" // Used for imports

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                selectedFileUri = uri
                // Pre-fill title with filename if possible
                val fileName = getFileName(applicationContext, uri)
                ebookTitleInput = fileName?.substringBeforeLast('.') ?: ""
                ebookAuthorInput = "" // Reset author
                showImportDialog = true
            }
        }
    )

    LaunchedEffect(subScriptId) {
        listViewModel.loadEbooks(subScriptId)
        settingsViewModel.loadFontSize(subScriptId)
    }

    val ebooks by listViewModel.ebooks.collectAsState()
    val currentFontSize by settingsViewModel.currentFontSize.collectAsState()

    if (showImportDialog && selectedFileUri != null) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Import Ebook") },
            text = {
                Column {
                    OutlinedTextField(
                        value = ebookTitleInput,
                        onValueChange = { ebookTitleInput = it },
                        label = { Text("Title") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = ebookAuthorInput,
                        onValueChange = { ebookAuthorInput = it },
                        label = { Text("Author (Optional)") }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            val uri = selectedFileUri!!
                            val tempFile = copyFileToCache(applicationContext, uri)
                            if (tempFile == null) {
                                Log.e("EbookListScreen", "Failed to copy file to cache")
                                showImportDialog = false
                                return@launch
                            }

                            val mimeType = getMimeType(applicationContext, uri)
                            Log.d("EbookListScreen", "Importing file: ${tempFile.absolutePath}, MIME: $mimeType")

                            val success = when {
                                mimeType == "text/plain" || tempFile.name.endsWith(".txt", ignoreCase = true) -> {
                                    ebookImporter.importTxtFile(
                                        filePath = tempFile.absolutePath,
                                        subScriptId = subScriptId,
                                        ebookTitle = ebookTitleInput,
                                        author = ebookAuthorInput.takeIf { it.isNotBlank() },
                                        databaseName = databaseName
                                    ) != null
                                }
                                mimeType == "application/pdf" || tempFile.name.endsWith(".pdf", ignoreCase = true) -> {
                                    ebookImporter.importPdfFile(
                                        filePath = tempFile.absolutePath,
                                        subScriptId = subScriptId,
                                        ebookTitle = ebookTitleInput,
                                        author = ebookAuthorInput.takeIf { it.isNotBlank() },
                                        databaseName = databaseName
                                    ) != null
                                }
                                else -> {
                                    Log.w("EbookListScreen", "Unsupported file type: $mimeType / ${tempFile.name}")
                                    false
                                }
                            }

                            if (success) {
                                listViewModel.loadEbooks(subScriptId) // Refresh list
                                Log.d("EbookListScreen", "Import successful, list refreshed.")
                            } else {
                                Log.e("EbookListScreen", "Import failed.")
                                // Optionally show a snackbar for failure
                            }
                            tempFile.delete() // Clean up temp file
                            showImportDialog = false
                        }
                    }
                ) { Text("Import") }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Ebooks for $subScriptId") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                Log.d("EbookListScreen", "FAB clicked - launching file picker")
                filePickerLauncher.launch(arrayOf("text/plain", "application/pdf"))
            }) {
                Icon(Icons.Filled.Add, contentDescription = "Import Ebook")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (ebooks.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No ebooks found, or still loading...")
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) { // Assign weight
                    items(ebooks) { ebook ->
                        EbookItem(ebook = ebook, onClick = {
                            Log.d("EbookListScreen", "Clicked on ebook: ${ebook.title}, ID: ${ebook.id}, SubScriptID: $subScriptId, DBName: $databaseName")
                            // Ensure databaseName is correctly propagated or reconstructed if needed
                            navController.navigate("ebook_reader_screen/$subScriptId/${ebook.id}/$databaseName")
                        })
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Font Settings UI
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Font Size: $currentFontSize", style = MaterialTheme.typography.bodyLarge)
                    Row {
                        IconButton(onClick = {
                            settingsViewModel.updateFontSize(subScriptId, currentFontSize - 2) // Decrease by 2, or your preferred step
                        }) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Decrease font size")
                        }
                        Spacer(modifier = Modifier.size(8.dp))
                        IconButton(onClick = {
                            settingsViewModel.updateFontSize(subScriptId, currentFontSize + 2) // Increase by 2
                        }) {
                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Increase font size")
                        }
                    }
                }
            }
             Spacer(modifier = Modifier.height(8.dp)) // Some spacing at the bottom
        }
    }
}

@Composable
fun EbookItem(ebook: Ebook, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = ebook.title, style = MaterialTheme.typography.titleMedium)
            ebook.author?.let {
                Text(text = "By: $it", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

class EbookListViewModelFactory(
    private val application: Application, // Changed from Context to Application
    private val subScriptsDataHelper: SubScriptsDataHelper
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EbookListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EbookListViewModel(application, subScriptsDataHelper) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.simpleName}")
    }
    companion object {}
}

// Factory for EbookSettingsViewModel
class EbookSettingsViewModelFactory(
    private val application: Application, // Changed from Context to Application
    private val subScriptsDataHelper: SubScriptsDataHelper
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EbookSettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EbookSettingsViewModel(application, subScriptsDataHelper) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.simpleName}")
    }
}
