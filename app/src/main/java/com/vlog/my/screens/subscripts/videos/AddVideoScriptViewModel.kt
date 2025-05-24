package com.vlog.my.screens.subscripts.videos

import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.util.UUID

// Reusing generic FileItem and NetworkItem structures, can be specialized if needed
data class VideoFileItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val uri: Uri
)

data class NetworkVideoItem(
    val id: String = UUID.randomUUID().toString(),
    var url: String
)

class AddVideoScriptViewModel : ViewModel() {
    val videoScriptTitle = mutableStateOf("")
    private val databaseName = mutableStateOf(UUID.randomUUID().toString()) // Auto-generated

    val localVideoFiles = mutableStateListOf<VideoFileItem>()
    val networkVideoUrls = mutableStateListOf<NetworkVideoItem>()

    fun addSelectedFile(uri: Uri, fileName: String) {
        localVideoFiles.add(VideoFileItem(name = fileName, uri = uri))
    }

    fun removeSelectedFile(item: VideoFileItem) {
        localVideoFiles.remove(item)
    }

    fun addNetworkVideoField() {
        networkVideoUrls.add(NetworkVideoItem(url = ""))
    }

    fun removeNetworkVideoField(item: NetworkVideoItem) {
        networkVideoUrls.remove(item)
    }
    
    fun updateNetworkVideoUrl(item: NetworkVideoItem, newUrl: String) {
       val index = networkVideoUrls.indexOf(item)
       if (index != -1) {
           networkVideoUrls[index] = item.copy(url = newUrl)
       }
    }

    fun saveVideoScript(onSuccess: () -> Unit) {
        viewModelScope.launch {
            // TODO: Implement actual saving logic for video script
            // Similar to music: SubScript entry, copy files, store metadata/URLs
            println("Saving Video Script: Title='${videoScriptTitle.value}', DB='${databaseName.value}'")
            println("Local Files: ${localVideoFiles.joinToString { it.name }}")
            println("Network URLs: ${networkVideoUrls.joinToString { it.url }}")
            onSuccess()
        }
    }
}
