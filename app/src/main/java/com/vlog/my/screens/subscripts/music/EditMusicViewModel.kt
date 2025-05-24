package com.vlog.my.screens.subscripts.music

import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.util.UUID

// Assuming MusicFileItem and NetworkMusicItem data classes are defined
// (e.g., in AddMusicViewModel.kt or a shared location)
// data class MusicFileItem(val id: String = UUID.randomUUID().toString(), val name: String, val uri: Uri)
// data class NetworkMusicItem(val id: String = UUID.randomUUID().toString(), var url: String)

class EditMusicViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val musicScriptId: String = savedStateHandle.get<String>("musicScriptId") ?: ""

    val musicTitle = mutableStateOf("")
    val databaseName = mutableStateOf("") // To be loaded, not editable

    val localMusicFiles = mutableStateListOf<MusicFileItem>()
    val networkMusicUrls = mutableStateListOf<NetworkMusicItem>()

    init {
        if (musicScriptId.isNotEmpty()) {
            loadMusicScriptDetails(musicScriptId)
        }
    }

    private fun loadMusicScriptDetails(id: String) {
        viewModelScope.launch {
            // TODO: Implement actual data loading logic
            // Fetch title, dbName, local file metadata (name, path/URI), network URLs
            println("Loading Music Script with ID: $id")
            // Placeholder data:
            musicTitle.value = "Sample Music Title for ID $id"
            databaseName.value = "db_uuid_for_$id" // This would be fetched
            localMusicFiles.addAll(listOf(
                MusicFileItem(name = "existing_track1.mp3", uri = Uri.EMPTY),
                MusicFileItem(name = "another_song.wav", uri = Uri.EMPTY)
            ))
            networkMusicUrls.addAll(listOf(
                NetworkMusicItem(url = "http://example.com/song.mp3"),
                NetworkMusicItem(url = "http://another.server/track.ogg")
            ))
        }
    }

    fun addSelectedFile(uri: Uri, fileName: String) {
        localMusicFiles.add(MusicFileItem(name = fileName, uri = uri))
    }

    fun removeSelectedFile(item: MusicFileItem) {
        // TODO: If file is already saved, mark for deletion or delete from storage
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

    fun updateMusicScript(onSuccess: () -> Unit) {
        viewModelScope.launch {
            // TODO: Implement actual update logic
            // Update title.
            // Handle newly added local files (copy to storage, add metadata).
            // Handle removed local files (delete from storage, remove metadata).
            // Update network URLs.
            // DatabaseName is not updated.
            println("Updating Music Script: ID='${musicScriptId}', Title='${musicTitle.value}'")
            println("Local Files: ${localMusicFiles.joinToString { it.name }}")
            println("Network URLs: ${networkMusicUrls.joinToString { it.url }}")
            onSuccess()
        }
    }
}
