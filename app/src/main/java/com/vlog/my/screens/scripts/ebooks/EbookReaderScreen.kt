package com.vlog.my.screens.scripts.ebooks

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EbookReaderScreen(
    navController: NavController,
    subScriptId: String,
    ebookId: String,
    databaseName: String
) {
    val application = LocalContext.current.applicationContext as Application
    val viewModel: EbookReaderViewModel = viewModel(
        factory = EbookReaderViewModelFactory(application, subScriptId, ebookId, databaseName)
    )

    val ebookDetails by viewModel.ebookDetails.collectAsState()
    val currentChapter by viewModel.currentChapter.collectAsState()
    val currentChapterContent by viewModel.currentChapterContent.collectAsState()
    val fontSize by viewModel.fontSize.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val chapters by viewModel.chapters.collectAsState() // For chapter navigation later

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(ebookDetails?.title ?: "Loading...") },
                navigationIcon = {
                    // IconButton(onClick = { navController.popBackStack() }) {
                    //     Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    // }
                },
                actions = {
                    // Placeholder for chapter selection DropdownMenu or other actions
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    currentChapter?.let {
                        Text(
                            text = it.title,
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    Text(
                        text = currentChapterContent,
                        fontSize = fontSize.sp,
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    )
                    // TODO: Add chapter navigation (e.g., Next/Previous buttons or a chapter list drawer)
                    // Example:
                    // Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    //     Button(onClick = { /* Find previous chapter and select */ }, enabled = /* condition */) { Text("Previous") }
                    //     Button(onClick = { /* Find next chapter and select */ }, enabled = /* condition */) { Text("Next") }
                    // }
                }
            }
        }
    }
}

class EbookReaderViewModelFactory(
    private val application: Application,
    private val subScriptId: String,
    private val ebookId: String,
    private val databaseName: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EbookReaderViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EbookReaderViewModel(application, subScriptId, ebookId, databaseName) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class for EbookReaderViewModelFactory")
    }
}
