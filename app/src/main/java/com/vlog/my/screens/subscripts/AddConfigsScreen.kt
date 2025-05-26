package com.vlog.my.screens.subscripts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.vlog.my.data.scripts.configs.MetasConfig
import kotlinx.coroutines.flow.collectLatest

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
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
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
            item { ConfigTextField(label = "API URL*", value = viewModel.apiUrlField.value, error = viewModel.apiUrlError.value, onValueChange = { viewModel.apiUrlField.value = it }) }
            item { ConfigTextField(label = "URL Parameters (Optional)", value = viewModel.urlParamsField.value, onValueChange = { viewModel.urlParamsField.value = it }) }
            item { ConfigTextField(label = "URL Type (Int, Default 0)", value = viewModel.urlTypedField.value.toString(), keyboardType = KeyboardType.Number, onValueChange = { viewModel.urlTypedField.value = it.toIntOrNull() ?: 0 }) }
            item { ConfigTextField(label = "Root Path*", value = viewModel.rootPath.value, error = viewModel.rootPathError.value, onValueChange = { viewModel.rootPath.value = it }) }

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
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        isError = error != null,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = keyboardType != KeyboardType.Text // Allow multiline for general text
    )
    error?.let {
        Text(text = it, color = MaterialTheme.colors.error, style = MaterialTheme.typography.caption)
    }
}

@Composable
fun MetaListEditor(
    title: String,
    metaList: MutableList<UIMetasConfig>,
    onAdd: () -> Unit,
    onRemove: (UIMetasConfig) -> Unit
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
fun MetaItemEditor(uiMeta: UIMetasConfig, onRemove: () -> Unit) {
    // Need to use remember for the individual text field states if UIMetasConfig properties are not mutableStateOf
    // Or, ensure UIMetasConfig uses mutableStateOf for its properties if direct modification is intended.
    // For this setup, AddConfigsViewModel.UIMetasConfig uses regular vars, so we manage state here or pass lambdas.
    // Let's assume direct modification of UIMetasConfig properties in ViewModel through list reference.

    var metaTypedState by remember { mutableStateOf(uiMeta.metaTyped.toString()) }
    var metaKeyState by remember { mutableStateOf(uiMeta.metaKey) }
    var metaValueState by remember { mutableStateOf(uiMeta.metaValue) }
    
    // Update ViewModel when local state changes
    LaunchedEffect(metaTypedState) { uiMeta.metaTyped = metaTypedState.toIntOrNull() ?: 0 }
    LaunchedEffect(metaKeyState) { uiMeta.metaKey = metaKeyState }
    LaunchedEffect(metaValueState) { uiMeta.metaValue = metaValueState }


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
                value = metaKeyState,
                onValueChange = { metaKeyState = it }
            )
            ConfigTextField(
                label = "Meta Value (Optional)",
                value = metaValueState,
                onValueChange = { metaValueState = it }
            )
        }
    }
}

@Composable
fun SectionSpacer() {
    Spacer(modifier = Modifier.height(16.dp))
}
