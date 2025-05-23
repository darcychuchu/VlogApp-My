package com.vlog.my.screens.videos

import android.net.Uri
import androidx.media3.common.C
import androidx.media3.common.DataReader
import androidx.media3.common.DataSource
import androidx.media3.common.DataSourceException
import androidx.media3.common.DataSpec
import java.io.IOException

class ByteArrayDataSource(private val data: ByteArray) : DataSource {

    private var uri: Uri? = null
    private var bytesRemaining: Int = 0
    private var opened: Boolean = false
    private var readPosition: Int = 0

    override fun addTransferListener(transferListener: androidx.media3.datasource.TransferListener) {
        // Not strictly necessary for this simple local data source if not tracking bandwidth, etc.
    }

    override fun open(dataSpec: DataSpec): Long {
        uri = dataSpec.uri
        if (dataSpec.position > data.size) {
            throw DataSourceException(PlaybackException.ERROR_CODE_IO_READ_POSITION_OUT_OF_RANGE)
        }
        readPosition = dataSpec.position.toInt()
        bytesRemaining = (data.size - dataSpec.position).toInt()

        if (dataSpec.length != C.LENGTH_UNSET.toLong()) {
            bytesRemaining = minOf(bytesRemaining, dataSpec.length.toInt())
        }

        opened = true
        // Listeners would be notified about transfer started here
        return bytesRemaining.toLong()
    }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        if (length == 0) {
            return 0
        }
        if (bytesRemaining == 0) {
            return C.RESULT_END_OF_INPUT
        }

        val bytesToRead = minOf(length, bytesRemaining)
        System.arraycopy(data, readPosition, buffer, offset, bytesToRead)
        readPosition += bytesToRead
        bytesRemaining -= bytesToRead
        // Listeners would be notified about bytes transferred here
        return bytesToRead
    }

    override fun getUri(): Uri? {
        return uri
    }

    override fun close() {
        if (opened) {
            opened = false
            // Listeners would be notified about transfer ended here
        }
        uri = null
    }

    // Factory class for creating ByteArrayDataSource instances
    class Factory(private val data: ByteArray) : androidx.media3.datasource.DataSource.Factory {
        override fun createDataSource(): DataSource {
            return ByteArrayDataSource(data)
        }
    }
}

// Minimal PlaybackException for API level < 21 if not using full media3 dependency with exceptions
private object PlaybackException {
    const val ERROR_CODE_IO_READ_POSITION_OUT_OF_RANGE = 2008 // Matches ExoPlayer's PlaybackException.ERROR_CODE_IO_READ_POSITION_OUT_OF_RANGE
}
