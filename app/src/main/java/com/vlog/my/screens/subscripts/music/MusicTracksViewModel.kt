package com.vlog.my.screens.subscripts.music

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vlog.my.data.scripts.music.MusicItem
import com.vlog.my.data.scripts.music.MusicScriptDataHelper
import android.content.Context
import android.content.Intent
import android.util.Log
import com.vlog.my.playback.MusicPlaybackService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

class MusicTracksViewModel(
    application: Application, // Keep Application type for context if needed, but prefer specific Context
    musicDatabaseName: String
) : ViewModel() {

    private val musicScriptDataHelper = MusicScriptDataHelper(application, musicDatabaseName)

    private val _musicTracks = MutableStateFlow<List<MusicItem>>(emptyList())
    val musicTracks: StateFlow<List<MusicItem>> = _musicTracks.asStateFlow()

    private val _currentlyPlayingTrackId = MutableStateFlow<String?>(null)
    val currentlyPlayingTrackId: StateFlow<String?> = _currentlyPlayingTrackId.asStateFlow()

    init {
        loadMusicTracks()
    }

    fun loadMusicTracks() {
        viewModelScope.launch {
            try {
                _musicTracks.value = musicScriptDataHelper.getAllMusicTracks()
            } catch (e: Exception) {
                Log.e("MusicTracksViewModel", "Error loading music tracks", e)
                // Optionally, expose an error state to the UI
            }
        }
    }

    fun addMusicTrack(title: String, artist: String?, album: String?, filePath: String?, url: String?, musicData: ByteArray?) {
        viewModelScope.launch {
            val newItem = MusicItem(
                id = UUID.randomUUID().toString(),
                title = title,
                artist = artist?.takeIf { it.isNotBlank() },
                album = album?.takeIf { it.isNotBlank() },
                filePath = filePath?.takeIf { it.isNotBlank() },
                url = url?.takeIf { it.isNotBlank() },
                musicData = musicData
            )
            try {
                musicScriptDataHelper.addMusicTrack(newItem)
                loadMusicTracks() // Refresh the list
            } catch (e: Exception) {
                // Log error
                Log.e("MusicTracksViewModel", "Error adding music track", e)
            }
        }
    }

    fun deleteMusicTrack(trackId: String) {
        viewModelScope.launch {
            try {
                musicScriptDataHelper.deleteMusicTrack(trackId)
                loadMusicTracks() // Refresh the list
            } catch (e: Exception) {
                Log.e("MusicTracksViewModel", "Error deleting music track $trackId", e)
            }
        }
    }

    fun updateMusicTrack(track: MusicItem) {
        viewModelScope.launch {
            try {
                musicScriptDataHelper.updateMusicTrack(track)
                loadMusicTracks() // Refresh the list
            } catch (e: Exception) {
                Log.e("MusicTracksViewModel", "Error updating music track ${track.id}", e)
            }
        }
    }

    private fun saveBlobToTempFile(trackId: String, musicData: ByteArray, context: Context): android.net.Uri? {
        val tempFile = File(context.cacheDir, "temp_audio_${trackId}.dat")
        return try {
            FileOutputStream(tempFile).use { fos ->
                fos.write(musicData)
            }
            android.net.Uri.fromFile(tempFile)
        } catch (e: IOException) {
            Log.e("MusicTracksViewModel", "Error saving BLOB to temp file for track $trackId", e)
            null
        }
    }

    fun prepareTrackForPlayback(track: MusicItem, context: Context) {
        _currentlyPlayingTrackId.value = track.id // Set playing track ID
        val intent = Intent(context, MusicPlaybackService::class.java)
        if (track.musicData != null) {
            val tempFileUri = saveBlobToTempFile(track.id, track.musicData, context)
            if (tempFileUri != null) {
                intent.action = MusicPlaybackService.ACTION_PREPARE_TRACK
                intent.putExtra(MusicPlaybackService.EXTRA_TRACK_URI, tempFileUri.toString())
                context.startService(intent)
            } else {
                Log.e("MusicTracksViewModel", "Failed to create temp file for track ${track.id}")
                _currentlyPlayingTrackId.value = null // Clear if preparation failed
            }
        } else if (!track.url.isNullOrEmpty()) {
            intent.action = MusicPlaybackService.ACTION_PREPARE_TRACK
            intent.putExtra(MusicPlaybackService.EXTRA_TRACK_URL, track.url)
            context.startService(intent)
        } else {
            Log.e("MusicTracksViewModel", "No data or URL for track ${track.id}")
            _currentlyPlayingTrackId.value = null // Clear if no data
        }
    }

    fun stopPlayback(context: Context) {
        val intent = Intent(context, MusicPlaybackService::class.java)
        intent.action = MusicPlaybackService.ACTION_STOP
        context.startService(intent)
        _currentlyPlayingTrackId.value = null // Clear the playing track ID
    }

    fun pausePlayback(context: Context) {
        val intent = Intent(context, MusicPlaybackService::class.java)
        intent.action = MusicPlaybackService.ACTION_PAUSE
        context.startService(intent)
        // Note: currentlyPlayingTrackId is not cleared on pause
    }

    fun playTrack(trackId: String, context: Context) {
        val intent = Intent(context, MusicPlaybackService::class.java)
        intent.action = MusicPlaybackService.ACTION_PLAY
        // Assuming service plays the currently prepared track, no need to pass trackId again
        // If service needed it: intent.putExtra(MusicPlaybackService.EXTRA_TRACK_ID, trackId)
        context.startService(intent)
        _currentlyPlayingTrackId.value = trackId // Ensure it's set
    }
}
