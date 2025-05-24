package com.vlog.my.screens.subscripts.music

import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.util.UUID

data class MusicFileItem(
    val id: String = UUID.randomUUID().toString(), // For list key
    val name: String,
    val uri: Uri
)

data class NetworkMusicItem(
    val id: String = UUID.randomUUID().toString(), // For list key
    var url: String
)

class AddMusicViewModel : ViewModel() {
    val musicTitle = mutableStateOf("")
    private val databaseName = mutableStateOf(UUID.randomUUID().toString()) // Auto-generated

    val localMusicFiles = mutableStateListOf<MusicFileItem>()
    val networkMusicUrls = mutableStateListOf<NetworkMusicItem>()

    fun addSelectedFile(uri: Uri, fileName: String) {
        localMusicFiles.add(MusicFileItem(name = fileName, uri = uri))
    }

    fun removeSelectedFile(item: MusicFileItem) {
        localMusicFiles.remove(item)
    }

    fun addNetworkMusicField() {
        networkMusicUrls.add(NetworkMusicItem(url = ""))
    }

    fun removeNetworkMusicField(item: NetworkMusicItem) {
        networkMusicUrls.remove(item)
    }
    
    fun updateNetworkMusicUrl(item: NetworkMusicItem, newUrl: String) {
       val index = networkMusicUrls.indexOf(item)
       if (index != -1) {
           networkMusicUrls[index] = item.copy(url = newUrl)
       }
    }

    fun saveMusicScript(onSuccess: () -> Unit) {
        viewModelScope.launch {
            // TODO: Implement actual saving logic for music script
            // This will involve:
            // 1. Getting current user for 'createdBy'
            // 2. Creating a SubScript entry (or similar) with type MUSIC
            // 3. Storing musicTitle, the auto-generated databaseName.
            // 4. Handling localMusicFiles:
            //    - Copy selected audio files to app-specific storage associated with this script/databaseName.
            //    - Store metadata (paths, titles) in the script's database.
            // 5. Handling networkMusicUrls:
            //    - Store these URLs in the script's database.
            println("Saving Music Script: Title='${musicTitle.value}', DB='${databaseName.value}'")
            println("Local Files: ${localMusicFiles.joinToString { it.name }}")
            println("Network URLs: ${networkMusicUrls.joinToString { it.url }}")
            onSuccess()
        }
    }
}
