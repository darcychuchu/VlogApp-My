package com.vlog.my.screens.subscripts.music

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MusicTracksViewModelFactory(
    private val application: Application,
    private val musicDatabaseName: String
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MusicTracksViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MusicTracksViewModel(application, musicDatabaseName) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
