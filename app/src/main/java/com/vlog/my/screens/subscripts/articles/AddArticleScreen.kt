package com.vlog.my.screens.subscripts.articles

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddArticleScreen(
    navController: NavController,
    viewModel: AddArticleViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Add New Article") },
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
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = viewModel.articleTitle,
                onValueChange = { viewModel.articleTitle = it },
                label = { Text("Article Title") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = viewModel.articleContent,
                onValueChange = { viewModel.articleContent = it },
                label = { Text("Article Content") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp), // Adjust height as needed
                singleLine = false
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = viewModel.articleAuthor,
                onValueChange = { viewModel.articleAuthor = it },
                label = { Text("Author (Optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // Optional: Database Name, similar to AddSubScriptsScreen if articles are per-script
            // OutlinedTextField(
            //     value = viewModel.articleDbName,
            //     onValueChange = { viewModel.articleDbName = it },
            //     label = { Text("Database Name (Optional)") },
            //     placeholder = { Text("Leave blank for default") },
            //     modifier = Modifier.fillMaxWidth()
            // )
            // Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.saveArticle {
                        navController.popBackStack() // Navigate back on success
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Article")
            }
        }
    }
}
