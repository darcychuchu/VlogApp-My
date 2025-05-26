package com.vlog.my.screens.subscripts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.vlog.my.data.scripts.configs.BasicsConfig

// Define navigation routes (can be moved to a central NavGraph file later)
const val ADD_CONFIGS_SCREEN_ROUTE = "AddConfigsScreen"
const val EDIT_CONFIGS_SCREEN_ROUTE_PREFIX = "EditConfigsScreen" // e.g., "EditConfigsScreen/{basicId}"

@Composable
fun ConfigsScreen(navController: NavController, configsViewModel: ConfigsViewModel = viewModel()) {
    val configsList by configsViewModel.configsList.collectAsState()
    val isLoading by configsViewModel.isLoading.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    // Call fetchConfigs when the screen is (re)composed or navigated back to.
    // This ensures data is refreshed from Add/Edit screens.
    LaunchedEffect(Unit) { // Use a key that might change if you need more specific re-triggering
        configsViewModel.fetchConfigs()
    }
    var selectedConfigIdToDelete by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Configurations") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate(ADD_CONFIGS_SCREEN_ROUTE)
            }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Configuration")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (configsList.isEmpty()) {
                Text(
                    text = "No configurations found. Tap the + button to add one.",
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                    style = MaterialTheme.typography.subtitle1
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(configsList, key = { it.basicId }) { config ->
                        ConfigItem(
                            config = config,
                            onEdit = { basicId ->
                                navController.navigate("$EDIT_CONFIGS_SCREEN_ROUTE_PREFIX/$basicId")
                            },
                            onDelete = { basicId ->
                                selectedConfigIdToDelete = basicId
                                showDialog = true
                            }
                        )
                        Divider()
                    }
                }
            }

            if (showDialog && selectedConfigIdToDelete != null) {
                AlertDialog(
                    onDismissRequest = {
                        showDialog = false
                        selectedConfigIdToDelete = null
                    },
                    title = { Text("Confirm Delete") },
                    text = { Text("Are you sure you want to delete this configuration?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                selectedConfigIdToDelete?.let { configsViewModel.deleteConfig(it) }
                                showDialog = false
                                selectedConfigIdToDelete = null
                            }
                        ) {
                            Text("Delete")
                        }
                    },
                    dismissButton = {
                        Button(onClick = {
                            showDialog = false
                            selectedConfigIdToDelete = null
                        }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ConfigItem(
    config: BasicsConfig,
    onEdit: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "API URL: ${config.apiUrlField}", style = MaterialTheme.typography.subtitle1)
            Text(text = "Scripts ID: ${config.scriptsId}", style = MaterialTheme.typography.body2)
            config.rootPath?.let {
                if(it.isNotEmpty()){
                     Text(text = "Root Path: $it", style = MaterialTheme.typography.caption)
                }
            }
        }
        IconButton(onClick = { onEdit(config.basicId) }) {
            Icon(Icons.Filled.Edit, contentDescription = "Edit")
        }
        IconButton(onClick = { onDelete(config.basicId) }) {
            Icon(Icons.Filled.Delete, contentDescription = "Delete")
        }
    }
}
// Mock NavController for preview if needed
// class MockNavController(context: Context) : NavController(context) 
// @Preview(showBackground = true)
// @Composable
// fun ConfigsScreenPreview() {
//     val context = LocalContext.current
//     // You'd need a mock ViewModel or pass mock data directly for a preview
//     ConfigsScreen(navController = NavController(context))
// }
