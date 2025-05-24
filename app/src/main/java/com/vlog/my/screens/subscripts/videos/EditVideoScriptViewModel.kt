package com.vlog.my.screens.subscripts.videos

import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.util.UUID

// Assuming VideoFileItem and NetworkVideoItem data classes are defined
// (e.g., in AddVideoScriptViewModel.kt or a shared location)
// data class VideoFileItem(val id: String = UUID.randomUUID().toString(), val name: String, val uri: Uri)
// data class NetworkVideoItem(val id: String = UUID.randomUUID().toString(), var url: String)

class EditVideoScriptViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val videoScriptId: String = savedStateHandle.get<String>("videoScriptId") ?: ""

    val videoScriptTitle = mutableStateOf("")
    val databaseName = mutableStateOf("") // To be loaded, not editable

    val localVideoFiles = mutableStateListOf<VideoFileItem>()
    val networkVideoUrls = mutableStateListOf<NetworkVideoItem>()

    init {
        if (videoScriptId.isNotEmpty()) {
            loadVideoScriptDetails(videoScriptId)
        }
    }

    private fun loadVideoScriptDetails(id: String) {
        viewModelScope.launch {
            // TODO: Implement actual data loading logic
            // Fetch title, dbName, local file metadata (name, path/URI), network URLs
            println("Loading Video Script with ID: $id")
            // Placeholder data:
            videoScriptTitle.value = "Sample Video Title for ID $id"
            databaseName.value = "db_uuid_for_video_$id"
            localVideoFiles.addAll(listOf(
                VideoFileItem(name = "existing_video.mp4", uri = Uri.EMPTY),
                VideoFileItem(name = "another_clip.mkv", uri = Uri.EMPTY)
            ))
            networkVideoUrls.addAll(listOf(
                NetworkVideoItem(url = "http://example.com/video.mp4"),
                NetworkVideoItem(url = "http://another.server/stream.m3u8")
            ))
        }
    }

    fun addSelectedFile(uri: Uri, fileName: String) {
        localVideoFiles.add(VideoFileItem(name = fileName, uri = uri))
    }

    fun removeSelectedFile(item: VideoFileItem) {
        // TODO: If file is already saved, mark for deletion or delete from storage
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

    fun updateVideoScript(onSuccess: () -> Unit) {
        viewModelScope.launch {
            // TODO: Implement actual update logic for video script
            println("Updating Video Script: ID='${videoScriptId}', Title='${videoScriptTitle.value}'")
            println("Local Files: ${localVideoFiles.joinToString { it.name }}")
            println("Network URLs: ${networkVideoUrls.joinToString { it.url }}")
            onSuccess()
        }
    }
}
