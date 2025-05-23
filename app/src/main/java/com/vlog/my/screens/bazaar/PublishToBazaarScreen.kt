package com.vlog.my.screens.bazaar

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import com.vlog.my.data.Result
import com.vlog.my.data.bazaar.BazaarScriptsRepository
import com.vlog.my.data.scripts.SubScripts
import com.vlog.my.data.scripts.SubScriptsDataHelper
import com.vlog.my.di.RetrofitModule // Assuming BazaarScriptsRepository is provided by Hilt or similar
import com.vlog.my.screens.users.UserViewModel // For user details

// Assuming ContentType.EBOOK.typeId is 4
private const val EBOOK_TYPE_ID = 4

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishToBazaarScreen(
    navController: NavController,
    subScriptId: String,
    userViewModel: UserViewModel = hiltViewModel() // For user token and name
    // ViewModel factory can be passed if not using Hilt for PublishToBazaarViewModel
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    // In a Hilt setup, BazaarScriptsRepository and SubScriptsDataHelper would be injected.
    // For manual setup, they need to be passed or constructed.
    // This is a simplified factory for demonstration.
    val publishViewModel: PublishToBazaarViewModel = viewModel(
        factory = PublishToBazaarViewModelFactory(
            application = application,
            // bazaarRepository = BazaarScriptsRepository(bazaarScriptsDao, bazaarScriptsService) // Replace with actual instantiation
            // For simplicity, assuming RetrofitModule can provide the service for the repo.
            // This part is highly dependent on your DI setup.
            bazaarRepository = BazaarScriptsRepository(
                bazaarScriptsDao = null, // This won't work if BazaarScriptsRepository needs a DAO for local ops not used here
                bazaarScriptsService = RetrofitModule.provideBazaarScriptsService(RetrofitModule.provideRetrofit()) // Example
            ),
            subScriptsDataHelper = SubScriptsDataHelper(application)
        )
    )

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var logoFilePath by remember { mutableStateOf("") } // For user input of logo path

    val subScriptState = remember { mutableStateOf<SubScripts?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val publishState by publishViewModel.publishState.collectAsState()

    val currentUser = userViewModel.currentUser.collectAsState().value
    val userToken = userViewModel.userToken.collectAsState(initial = null).value // Or however you get the token

    LaunchedEffect(subScriptId) {
        val helper = SubScriptsDataHelper(context)
        subScriptState.value = helper.getUserScriptsById(subScriptId)
        // Pre-fill title if desired, e.g., title = subScriptState.value?.name ?: ""
    }

    LaunchedEffect(publishState) {
        when (val result = publishState) {
            is Result.Success -> {
                snackbarHostState.showSnackbar("Published successfully!")
                navController.popBackStack() // Navigate back on success
            }
            is Result.Error -> {
                snackbarHostState.showSnackbar("Error: ${result.exception.message}")
            }
            Result.Loading -> {
                // Handled by CircularProgressIndicator below
            }
            null -> {} // Initial state
        }
        // Reset state in ViewModel if you want the Snackbar to show again on re-publish attempt after error
         if (publishState !is Result.Loading && publishState != null) {
             publishViewModel.resetPublishState()
         }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(title = { Text("Publish '${subScriptState.value?.name ?: "Script"}' to Bazaar") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (subScriptState.value == null) {
                Text("Loading script details...")
                return@Scaffold
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title (Public Name)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            OutlinedTextField(
                value = tags,
                onValueChange = { tags = it },
                label = { Text("Tags (comma-separated)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = logoFilePath,
                onValueChange = { logoFilePath = it },
                label = { Text("Logo File Path (local)") },
                placeholder = {Text("e.g. /path/to/your/logo.png or leave empty for default/no logo")},
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (currentUser == null || userToken == null) {
                        Log.e("PublishScreen", "User or token is null. Cannot publish.")
                        // Show Snackbar or some error to user.
                        // This might indicate an issue with login state.
                        scope.launch { snackbarHostState.showSnackbar("Error: User not logged in or token missing.")}
                        return@Button
                    }
                    if (subScriptState.value == null) {
                         scope.launch { snackbarHostState.showSnackbar("Error: SubScript details not loaded.")}
                        return@Button
                    }

                    Log.d("PublishScreen", "Publishing with: subScriptId=$subScriptId, title=$title, desc=$description, tags=$tags, logo=$logoFilePath, user=${currentUser.name}, tokenPresent=${userToken!=null}")

                    publishViewModel.publishScript(
                        subScriptId = subScriptId,
                        title = title.ifBlank { subScriptState.value?.name ?: "Untitled" },
                        description = description,
                        tags = tags,
                        logoFilePath = logoFilePath.takeIf { it.isNotBlank() },
                        currentUserName = currentUser.name, // from UserViewModel
                        userToken = userToken // from UserViewModel
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = publishState !is Result.Loading && subScriptState.value != null && currentUser != null && userToken != null
            ) {
                if (publishState is Result.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Publish to Bazaar")
                }
            }
        }
    }
}


// Simplified ViewModel Factory for PublishToBazaarViewModel
// In a real app, use Hilt or a more robust DI solution.
class PublishToBazaarViewModelFactory(
    private val application: Application,
    private val bazaarRepository: BazaarScriptsRepository,
    private val subScriptsDataHelper: SubScriptsDataHelper
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PublishToBazaarViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PublishToBazaarViewModel(application, bazaarRepository, subScriptsDataHelper) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class for PublishToBazaarViewModelFactory")
    }
}

// Dummy scope for snackbar outside button, replace with rememberCoroutineScope if needed inside Composable directly
val scope = kotlinx.coroutines.MainScope()
