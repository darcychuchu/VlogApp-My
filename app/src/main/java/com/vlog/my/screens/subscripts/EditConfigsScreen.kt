package com.vlog.my.screens.subscripts

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.flow.collectLatest
import com.vlog.my.data.scripts.configs.MetasConfig // Needed for EditableMetaItem
import java.util.UUID // Needed for EditableMetaItem default clientSideId

// --- Start of Duplicated/Shared Composables from AddConfigsScreen ---
// In a real project, these would be in a shared UI components file.

@Composable
private fun RecursiveMetasConfigView(
    label: String,
    metaItemList: MutableList<EditableMetaItem>,
    viewModel: EditConfigsViewModel, 
    targetListType: String, 
    level: Int = 0
) {
    Column(modifier = Modifier.padding(start = (level * 16).dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = if (level == 0) label else "Child Metas", 
                style = if (level == 0) MaterialTheme.typography.subtitle1 else MaterialTheme.typography.subtitle2,
                modifier = Modifier.weight(1f)
            )
            if (level == 0) {
                IconButton(onClick = {
                    val list = if (targetListType == "basic") viewModel.basicEditableMetas else viewModel.fieldEditableMetas
                    viewModel.addMetaItem(list, null) { // parentClientSideId is null for root items
                        // quoteIdProducer logic for EditConfigsViewModel
                        if (targetListType == "basic") {
                            viewModel.basicId.value // This is a State<String>, so .value is correct
                        } else {
                            viewModel.fieldId.value ?: UUID.randomUUID().toString() // Use existing fieldId or a new one if it's somehow null (should be rare if hasFieldsConfig is true)
                        }
                    }
                }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Root Meta for $label")
                }
            }
        }

        metaItemList.forEach { item ->
            EditableMetaItemView(
                editableMetaItem = item,
                viewModel = viewModel,
                allBasicMetas = viewModel.basicEditableMetas, 
                allFieldMetas = viewModel.fieldEditableMetas, 
                level = level,
                targetListType = targetListType // Pass targetListType for recursive calls
            ) {
                // Add child to current item
                val listContext = if (targetListType == "basic") viewModel.basicEditableMetas else viewModel.fieldEditableMetas
                viewModel.addMetaItem(listContext, item.clientSideId) { item.metasConfig.quoteId } // Child inherits quoteId
            }
        }
        if (metaItemList.isEmpty() && level > 0) { 
             Text("No child metas.", style = MaterialTheme.typography.caption, modifier = Modifier.padding(start = 8.dp, top = 4.dp))
        }
         if (metaItemList.isEmpty() && level == 0) {
            Text("No metas added for $label.", style = MaterialTheme.typography.caption, modifier = Modifier.padding(start = 8.dp, top = 4.dp))
        }
    }
}

@Composable
private fun EditableMetaItemView(
    editableMetaItem: EditableMetaItem,
    viewModel: EditConfigsViewModel, 
    allBasicMetas: MutableList<EditableMetaItem>, 
    allFieldMetas: MutableList<EditableMetaItem>, 
    level: Int,
    targetListType: String, // Added to know which list to use for recursive calls
    onAddChild: () -> Unit
) {
    var metaKey by remember(editableMetaItem.clientSideId, editableMetaItem.metasConfig.metaKey) { mutableStateOf(editableMetaItem.metasConfig.metaKey ?: "") }
    var metaValue by remember(editableMetaItem.clientSideId, editableMetaItem.metasConfig.metaValue) { mutableStateOf(editableMetaItem.metasConfig.metaValue ?: "") }
    var metaTyped by remember(editableMetaItem.clientSideId, editableMetaItem.metasConfig.metaTyped) { mutableStateOf((editableMetaItem.metasConfig.metaTyped ?: 0).toString()) }

    // Determine which root list this item (or its ancestor) belongs to for property updates and removal
    val rootListForOperations = if (targetListType == "basic") allBasicMetas else allFieldMetas
    
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp, horizontal = (level * 4).dp), elevation = (2+level).dp) {
        Column(modifier = Modifier.padding(8.dp)) {
            ConfigTextField(
                label = "Meta Key", value = metaKey,
                onValueChange = {
                    metaKey = it
                    viewModel.updateMetaItemProperties(rootListForOperations, editableMetaItem.clientSideId, metaKey, metaValue, metaTyped.toIntOrNull() ?: 0)
                }
            )
            ConfigTextField(
                label = "Meta Value", value = metaValue,
                onValueChange = {
                    metaValue = it
                    viewModel.updateMetaItemProperties(rootListForOperations, editableMetaItem.clientSideId, metaKey, metaValue, metaTyped.toIntOrNull() ?: 0)
                }
            )
            ConfigTextField(
                label = "Meta Type (Int)", value = metaTyped, keyboardType = KeyboardType.Number,
                onValueChange = {
                    metaTyped = it
                    viewModel.updateMetaItemProperties(rootListForOperations, editableMetaItem.clientSideId, metaKey, metaValue, metaTyped.toIntOrNull() ?: 0)
                }
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onAddChild) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Child Meta", modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add Child")
                }
                IconButton(onClick = {
                     viewModel.removeMetaItem(rootListForOperations, editableMetaItem.clientSideId)
                }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Remove This Meta")
                }
            }
            if (editableMetaItem.children.isNotEmpty()) {
                 RecursiveMetasConfigView(
                    label = "Child Metas", 
                    metaItemList = editableMetaItem.children,
                    viewModel = viewModel,
                    targetListType = targetListType, // Pass down the same type
                    level = level + 1
                    // parentClientSideId for adding children is handled by onAddChild lambda context
                )
            }
        }
    }
}

// Removed findEditableMetaRecursive from here as it's private in ViewModel
// and ViewModel methods are used for operations from UI.

// --- End of Duplicated/Shared Composables ---


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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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



                    // Basic Metas - Use RecursiveMetasConfigView
                    item { SectionSpacer() }
                    item {
                        RecursiveMetasConfigView(
                            label = "Basic Metas (Optional)",
                            metaItemList = viewModel.basicEditableMetas,
                            viewModel = viewModel,
                            targetListType = "basic"
                        )
                    }


                    item { SectionSpacer() }
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Fields Configuration", style = MaterialTheme.typography.h6, modifier = Modifier.weight(1f))
                            Checkbox(checked = viewModel.hasFieldsConfig.value, onCheckedChange = {
                                viewModel.hasFieldsConfig.value = it
                                if (!it) {
                                    viewModel.fieldId.value = null 
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


                        // Field Metas (only if FieldsConfig is enabled) - Use RecursiveMetasConfigView
                        item { SectionSpacer() }
                        item {
                            RecursiveMetasConfigView(
                                label = "Field Metas (Optional)",
                                metaItemList = viewModel.fieldEditableMetas,
                                viewModel = viewModel,
                                targetListType = "field"
                            )
                        }

                    }

                    // Field Metas (only if FieldsConfig is enabled) - Use RecursiveMetasConfigView
//                    if (viewModel.hasFieldsConfig.value) {
//
//                    }

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
