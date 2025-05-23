package com.vlog.my.screens.videos

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class AddVideoViewModelFactory(
    private val application: Application,
    private val databaseName: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddVideoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddVideoViewModel(application, databaseName) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
