package com.vlog.my.screens.subscripts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
// MetasConfig is still needed by EditableMetaItem
import com.vlog.my.data.scripts.configs.MetasConfig 
import kotlinx.coroutines.flow.collectLatest
import java.util.UUID
//
//// Define RecursiveMetasConfigView and its helper MetaItemView here or in a shared UI file

@Composable
private fun RecursiveMetasConfigView(
    label: String,
    metaItemList: MutableList<EditableMetaItem>,
    viewModel: AddConfigsViewModel,
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
                        (if (targetListType == "basic") {
                            viewModel.basicId.value // This is a State<String>, so .value is correct
                        } else {
                            viewModel.fieldId.value ?: UUID.randomUUID().toString() // Use existing fieldId or a new one if it's somehow null (should be rare if hasFieldsConfig is true)
                        }).toString()
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
    viewModel: AddConfigsViewModel,
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

// Removed findParent as ViewModel's removeMetaItem and updateMetaItemProperties search from root lists.

@Composable
fun AddConfigsScreen(
    navController: NavController,
    viewModel: AddConfigsViewModel = viewModel()
) {
    val scaffoldState = rememberScaffoldState()

    LaunchedEffect(Unit) {
        viewModel.saveResult.collectLatest { success ->
            if (success) {
                navController.popBackStack()
            } else {
                scaffoldState.snackbarHostState.showSnackbar("Failed to save configuration.")
            }
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text("Add Configuration") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { Text("Basic Configuration", style = MaterialTheme.typography.h6) }

            item {
                ConfigTextField(
                    label = "API URL*",
                    value = viewModel.apiUrlField.value,
                    error = viewModel.apiUrlError.value,
                    onValueChange = { viewModel.apiUrlField.value = it },
                    trailingIcon = {
                        IconButton(onClick = { viewModel.initiateApiFormatting() }) { // Pass current URL via ViewModel property
                            Icon(Icons.Filled.AutoAwesome, contentDescription = "Format API URL")
                        }
                    }
                )
            }
            item { ConfigTextField(label = "API URL*", value = viewModel.apiUrlField.value, error = viewModel.apiUrlError.value, onValueChange = { viewModel.apiUrlField.value = it }) }
            item { ConfigTextField(label = "URL Parameters (Optional)", value = viewModel.urlParamsField.value, onValueChange = { viewModel.urlParamsField.value = it }) }
            item { ConfigTextField(label = "URL Type (Int, Default 0)", value = viewModel.urlTypedField.value.toString(), keyboardType = KeyboardType.Number, onValueChange = { viewModel.urlTypedField.value = it.toIntOrNull() ?: 0 }) }
            item { ConfigTextField(label = "Root Path*", value = viewModel.rootPath.value, error = viewModel.rootPathError.value, onValueChange = { viewModel.rootPath.value = it }) }

            // Basic Metas
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
                    Checkbox(checked = viewModel.hasFieldsConfig.value, onCheckedChange = { viewModel.hasFieldsConfig.value = it })
                    Text("Enable FieldsConfig")
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
//            if (viewModel.hasFieldsConfig.value) {
//
//            }

//            item { MetaListEditor("Basic Metas (Optional)", viewModel.basicEditableMetas, onAdd = {
//                //viewModel.addMetaItem()
//                viewModel.addMetaItem(viewModel.basicEditableMetas, null) { viewModel.basicId.value ?: UUID.randomUUID().toString()}
//            }, onRemove = {
//            }) }
//
//            // Field Metas (only if FieldsConfig is enabled)
//            if (viewModel.hasFieldsConfig.value) {
//                item { SectionSpacer() }
//                item { MetaListEditor("Field Metas (Optional)", viewModel.fieldEditableMetas, onAdd = {
//                    //viewModel.addFieldMeta()
//                }, onRemove = {
//                    //viewModel.removeFieldMeta(it)
//                }) }
//            }
            
            item { SectionSpacer() }
            item {
                Button(
                    onClick = { viewModel.saveConfig() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Configuration")
                }
            }
        }
    }
}

@Composable
fun ConfigTextField(
    label: String,
    value: String,
    error: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit,
    trailingIcon: @Composable (() -> Unit)? = null // Added trailingIcon parameter
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        isError = error != null,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = keyboardType != KeyboardType.Text, // Allow multiline for general text
        trailingIcon = trailingIcon // Pass trailingIcon to OutlinedTextField
    )
    error?.let {
        Text(text = it, color = MaterialTheme.colors.error, style = MaterialTheme.typography.caption)
    }
}

@Composable
fun MetaListEditor(
    title: String,
    metaList: MutableList<EditableMetaItem>,
    onAdd: () -> Unit,
    onRemove: (EditableMetaItem) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = MaterialTheme.typography.subtitle1, modifier = Modifier.weight(1f))
            IconButton(onClick = onAdd) {
                Icon(Icons.Filled.Add, contentDescription = "Add Meta")
            }
        }
        metaList.forEachIndexed { index, uiMeta ->
            MetaItemEditor(uiMeta = uiMeta, onRemove = { onRemove(uiMeta) })
        }
        if (metaList.isEmpty()) {
            Text("No metas added.", style = MaterialTheme.typography.caption, modifier = Modifier.padding(start = 8.dp))
        }
    }
}

@Composable
fun MetaItemEditor(uiMeta: EditableMetaItem, onRemove: () -> Unit) {
    // Need to use remember for the individual text field states if UIMetasConfig properties are not mutableStateOf
    // Or, ensure UIMetasConfig uses mutableStateOf for its properties if direct modification is intended.
    // For this setup, AddConfigsViewModel.UIMetasConfig uses regular vars, so we manage state here or pass lambdas.
    // Let's assume direct modification of UIMetasConfig properties in ViewModel through list reference.

    var metaTypedState by remember { mutableStateOf(uiMeta.metasConfig.metaTyped.toString()) }
    var metaKeyState by remember { mutableStateOf(uiMeta.metasConfig.metaKey) }
    var metaValueState by remember { mutableStateOf(uiMeta.metasConfig.metaValue) }
    
    // Update ViewModel when local state changes
    LaunchedEffect(metaTypedState) { uiMeta.metasConfig.metaTyped = metaTypedState.toIntOrNull() ?: 0 }
    LaunchedEffect(metaKeyState) { uiMeta.metasConfig.metaKey = metaKeyState }
    LaunchedEffect(metaValueState) { uiMeta.metasConfig.metaValue = metaValueState }


    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), elevation = 2.dp) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Meta Item", style = MaterialTheme.typography.subtitle2, modifier = Modifier.weight(1f))
                IconButton(onClick = onRemove) {
                    Icon(Icons.Filled.Delete, contentDescription = "Remove Meta")
                }
            }
            ConfigTextField(
                label = "Meta Type (Int, Default 0)",
                value = metaTypedState,
                keyboardType = KeyboardType.Number,
                onValueChange = { metaTypedState = it }
            )
            ConfigTextField(
                label = "Meta Key (Optional)",
                value = metaKeyState.toString(),
                onValueChange = { metaKeyState = it }
            )
            ConfigTextField(
                label = "Meta Value (Optional)",
                value = metaValueState.toString(),
                onValueChange = { metaValueState = it }
            )
        }
    }
}

@Composable
fun SectionSpacer() {
    Spacer(modifier = Modifier.height(16.dp))
}
