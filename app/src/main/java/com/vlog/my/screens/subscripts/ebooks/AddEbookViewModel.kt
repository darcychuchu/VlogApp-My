package com.vlog.my.screens.subscripts.ebooks

import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.util.UUID

data class EbookFileItem( // Specifically for .txt files
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val uri: Uri
)

data class NetworkEbookItem(
    val id: String = UUID.randomUUID().toString(),
    var url: String
)

class AddEbookViewModel : ViewModel() {
    val ebookScriptTitle = mutableStateOf("")
    private val databaseName = mutableStateOf(UUID.randomUUID().toString()) // Auto-generated

    val localEbookFiles = mutableStateListOf<EbookFileItem>()
    val networkEbookUrls = mutableStateListOf<NetworkEbookItem>()

    fun addSelectedFile(uri: Uri, fileName: String) {
        // Potentially add validation here for .txt if mime type isn't enough
        localEbookFiles.add(EbookFileItem(name = fileName, uri = uri))
    }

    fun removeSelectedFile(item: EbookFileItem) {
        localEbookFiles.remove(item)
    }

    fun addNetworkEbookField() {
        networkEbookUrls.add(NetworkEbookItem(url = ""))
    }

    fun removeNetworkEbookField(item: NetworkEbookItem) {
        networkEbookUrls.remove(item)
    }
    
    fun updateNetworkEbookUrl(item: NetworkEbookItem, newUrl: String) {
       val index = networkEbookUrls.indexOf(item)
       if (index != -1) {
           networkEbookUrls[index] = item.copy(url = newUrl)
       }
    }

    fun saveEbookScript(onSuccess: () -> Unit) {
        viewModelScope.launch {
            // TODO: Implement actual saving logic for eBook script
            // Similar to music/video: SubScript entry, copy .txt files, store metadata/URLs.
            // Ensure EbookImporter's importTxtFile is used for processing .txt files.
            println("Saving eBook Script: Title='${ebookScriptTitle.value}', DB='${databaseName.value}'")
            println("Local Files: ${localEbookFiles.joinToString { it.name }}")
            println("Network URLs: ${networkEbookUrls.joinToString { it.url }}")
            onSuccess()
        }
    }
}
