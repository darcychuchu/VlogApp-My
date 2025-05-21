package com.vlog.my.screens.subscripts.workers

import android.content.Context
import android.util.Log
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class WorkManagerInitializer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun initialize() {
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.INFO)
            .build()
        
        WorkManager.initialize(context, config)
    }
}