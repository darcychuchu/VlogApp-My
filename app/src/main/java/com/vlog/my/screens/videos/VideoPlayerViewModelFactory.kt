package com.vlog.my.screens.videos

import android.app.Application
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class VideoPlayerViewModelFactory(
    private val application: Application,
    private val databaseName: String?, // Nullable if playing from URL
    private val initialVideoId: String?, // Nullable if playing from URL
    private val initialVideoUrl: String? // New parameter for URL playback
) : AbstractSavedStateViewModelFactory() {
    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        if (modelClass.isAssignableFrom(VideoPlayerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VideoPlayerViewModel(application, databaseName, initialVideoId, initialVideoUrl, handle) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
