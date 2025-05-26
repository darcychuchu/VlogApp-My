package com.vlog.my.screens.subscripts

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.flow.collectLatest

@Composable
fun EditConfigsScreen(
    navController: NavController,
    basicId: String,
    viewModel: EditConfigsViewModel = viewModel(
        factory = EditConfigsViewModelFactory(LocalContext.current.applicationContext as Application, basicId)
    )
) {
    val scaffoldState = rememberScaffoldState()
    val isLoading by viewModel.isLoading.collectAsState()
    val configNotFound by viewModel.configNotFound.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.updateResult.collectLatest { success ->
            if (success) {
                navController.popBackStack() // Go back to ConfigsScreen
            } else {
                scaffoldState.snackbarHostState.showSnackbar("Failed to update configuration.")
            }
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text("Edit Configuration") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            configNotFound -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Text("Configuration not found.", style = MaterialTheme.typography.h6)
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item { Text("Basic Configuration (ID: ${viewModel.basicId.value})", style = MaterialTheme.typography.h6) }
                    item { Text("Scripts ID: ${viewModel.scriptsId.value} (Fixed)", style = MaterialTheme.typography.subtitle2) }

                    item { ConfigTextField(label = "API URL*", value = viewModel.apiUrlField.value, error = viewModel.apiUrlError.value, onValueChange = { viewModel.apiUrlField.value = it }) }
                    item { ConfigTextField(label = "URL Parameters (Optional)", value = viewModel.urlParamsField.value, onValueChange = { viewModel.urlParamsField.value = it }) }
                    item { ConfigTextField(label = "URL Type (Int, Default 0)", value = viewModel.urlTypedField.value.toString(), keyboardType = KeyboardType.Number, onValueChange = { viewModel.urlTypedField.value = it.toIntOrNull() ?: 0 }) }
                    item { ConfigTextField(label = "Root Path*", value = viewModel.rootPath.value, error = viewModel.rootPathError.value, onValueChange = { viewModel.rootPath.value = it }) }

                    item { SectionSpacer() }
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Fields Configuration", style = MaterialTheme.typography.h6, modifier = Modifier.weight(1f))
                            Checkbox(checked = viewModel.hasFieldsConfig.value, onCheckedChange = {
                                viewModel.hasFieldsConfig.value = it
                                // If toggling off, you might want to clear fields or handle data retention logic in ViewModel
                                if (!it) {
                                    viewModel.fieldId.value = null // Clear fieldId if disabled
                                }
                            })
                            Text("Enable FieldsConfig")
                        }
                         viewModel.fieldId.value?.let {
                            Text("FieldsConfig ID: $it (Auto-managed)", style = MaterialTheme.typography.caption)
                        }
                    }

                    if (viewModel.hasFieldsConfig.value) {
                        item { ConfigTextField(label = "ID Field Mapping*", value = viewModel.idField.value, error = viewModel.idFieldError.value, onValueChange = { viewModel.idField.value = it }) }
                        item { ConfigTextField(label = "Title Field Mapping*", value = viewModel.titleField.value, error = viewModel.titleFieldError.value, onValueChange = { viewModel.titleField.value = it }) }
                        item { ConfigTextField(label = "Picture Field (Optional)", value = viewModel.picField.value, onValueChange = { viewModel.picField.value = it }) }
                        item { ConfigTextField(label = "Content Field (Optional)", value = viewModel.contentField.value, onValueChange = { viewModel.contentField.value = it }) }
                        item { ConfigTextField(label = "Tags Field (Optional)", value = viewModel.tagsField.value, onValueChange = { viewModel.tagsField.value = it }) }
                        item { ConfigTextField(label = "Source URL Field (Optional)", value = viewModel.sourceUrlField.value, onValueChange = { viewModel.sourceUrlField.value = it }) }
                    }

                    // Basic Metas
                    item { SectionSpacer() }
                    item { MetaListEditor("Basic Metas (Optional)", viewModel.basicMetas, onAdd = { viewModel.addBasicMeta() }, onRemove = { viewModel.removeBasicMeta(it) }) }

                    // Field Metas (only if FieldsConfig is enabled)
                    if (viewModel.hasFieldsConfig.value) {
                        item { SectionSpacer() }
                        item { MetaListEditor("Field Metas (Optional)", viewModel.fieldMetas, onAdd = { viewModel.addFieldMeta() }, onRemove = { viewModel.removeFieldMeta(it) }) }
                    }

                    item { SectionSpacer() }
                    item {
                        Button(
                            onClick = { viewModel.updateConfig() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Update Configuration")
                        }
                    }
                }
            }
        }
    }
}
// Note: ConfigTextField, MetaListEditor, MetaItemEditor, SectionSpacer
// are assumed to be identical to those in AddConfigsScreen.kt.
// If they are not in a shared file, they should be copied here or refactored.
// For this exercise, assume they are available (e.g. copied from AddConfigsScreen.kt or in a shared UI components file).
// The UIMetasConfig data class is also assumed to be available (defined in AddConfigsViewModel or a shared location).
