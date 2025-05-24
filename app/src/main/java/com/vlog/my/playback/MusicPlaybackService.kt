package com.vlog.my.playback

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.IBinder
import android.util.Log

class MusicPlaybackService : Service(), MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    private var mediaPlayer: MediaPlayer? = null

    companion object {
        const val ACTION_PREPARE_TRACK = "com.vlog.my.playback.ACTION_PREPARE_TRACK"
        const val ACTION_PLAY = "com.vlog.my.playback.ACTION_PLAY"
        const val ACTION_PAUSE = "com.vlog.my.playback.ACTION_PAUSE"
        const val ACTION_STOP = "com.vlog.my.playback.ACTION_STOP"

        const val EXTRA_TRACK_URI = "com.vlog.my.playback.EXTRA_TRACK_URI"
        const val EXTRA_TRACK_URL = "com.vlog.my.playback.EXTRA_TRACK_URL"
    }

    override fun onCreate() {
        super.onCreate()
        // Initialization can be done here or deferred to onStartCommand for ACTION_PREPARE_TRACK
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        Log.d("MusicPlaybackService", "Received action: $action")

        when (action) {
            ACTION_PREPARE_TRACK -> {
                mediaPlayer?.release()
                mediaPlayer = MediaPlayer()
                mediaPlayer?.setOnPreparedListener(this)
                mediaPlayer?.setOnCompletionListener(this)
                mediaPlayer?.setOnErrorListener(this)

                val trackUriString = intent.extras?.getString(EXTRA_TRACK_URI)
                val trackUrl = intent.extras?.getString(EXTRA_TRACK_URL)

                try {
                    if (trackUriString != null) {
                        val trackUri = Uri.parse(trackUriString)
                        Log.d("MusicPlaybackService", "Preparing track from URI: $trackUri")
                        mediaPlayer?.setDataSource(applicationContext, trackUri)
                    } else if (trackUrl != null) {
                        Log.d("MusicPlaybackService", "Preparing track from URL: $trackUrl")
                        mediaPlayer?.setDataSource(trackUrl)
                    } else {
                        Log.e("MusicPlaybackService", "No track URI or URL provided for ACTION_PREPARE_TRACK")
                        stopSelf() // Stop if no data to play
                        return START_NOT_STICKY
                    }
                    mediaPlayer?.prepareAsync()
                } catch (e: Exception) {
                    Log.e("MusicPlaybackService", "Error setting data source", e)
                    mediaPlayer?.release()
                    mediaPlayer = null
                    stopSelf()
                }
            }
            ACTION_PLAY -> {
                Log.d("MusicPlaybackService", "Action PLAY received")
                if (mediaPlayer != null && mediaPlayer?.isPlaying == false) { // Check for null
                    mediaPlayer?.start()
                }
            }
            ACTION_PAUSE -> {
                Log.d("MusicPlaybackService", "Action PAUSE received")
                if (mediaPlayer != null && mediaPlayer?.isPlaying == true) { // Check for null
                    mediaPlayer?.pause()
                }
            }
            ACTION_STOP -> {
                Log.d("MusicPlaybackService", "Action STOP received")
                if (mediaPlayer != null) {
                    if (mediaPlayer!!.isPlaying) { // Check for null before accessing isPlaying
                        mediaPlayer?.stop()
                    }
                    mediaPlayer?.release()
                    mediaPlayer = null
                }
                stopSelf() // Stop the service when playback is stopped
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MusicPlaybackService", "Service Destroyed")
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null // Not a bound service
    }

    // MediaPlayer Listeners
    override fun onPrepared(mp: MediaPlayer?) {
        Log.d("MusicPlaybackService", "MediaPlayer prepared. Auto-playing.")
        mp?.start() // Auto-play when prepared as per revised requirement
        // Optionally, you could send a broadcast here to update UI or wait for an explicit PLAY action.
    }

    override fun onCompletion(mp: MediaPlayer?) {
        Log.d("MusicPlaybackService", "MediaPlayer playback completed.")
        mediaPlayer?.release()
        mediaPlayer = null
        // stopSelf() // Stop service on completion, or manage for playlist
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        Log.e("MusicPlaybackService", "MediaPlayer error: What: $what, Extra: $extra")
        mediaPlayer?.release()
        mediaPlayer = null
        // stopSelf() // Stop service on error
        return true // Indicates error was handled
    }
}
