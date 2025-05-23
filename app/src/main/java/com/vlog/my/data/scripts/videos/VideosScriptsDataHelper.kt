package com.vlog.my.data.scripts.videos

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// Define VideoMetadata data class here or in a separate file
data class VideoMetadata(
    val id: String,
    val title: String,
    val originalFileName: String?,
    val durationMs: Long?,
    val resolutionWidth: Int?,
    val resolutionHeight: Int?,
    val originalSizeBytes: Long?,
    val compressedSizeBytes: Long?,
    val mimeType: String?,
    val dateAdded: Long
)

class VideosScriptsDataHelper(context: Context, databaseName: String) :
    SQLiteOpenHelper(context, databaseName, null, 1 /* DATABASE_VERSION */) {

    companion object {
        const val TABLE_VIDEO_METADATA = "video_metadata"
        const val COLUMN_ID = "id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_ORIGINAL_FILE_NAME = "original_file_name"
        const val COLUMN_DURATION_MS = "duration_ms"
        const val COLUMN_RESOLUTION_WIDTH = "resolution_width"
        const val COLUMN_RESOLUTION_HEIGHT = "resolution_height"
        const val COLUMN_ORIGINAL_SIZE_BYTES = "original_size_bytes"
        const val COLUMN_COMPRESSED_SIZE_BYTES = "compressed_size_bytes"
        const val COLUMN_MIME_TYPE = "mime_type"
        const val COLUMN_DATE_ADDED = "date_added"

        const val TABLE_VIDEO_DATA = "video_data"
        const val COLUMN_VIDEO_METADATA_ID = "video_metadata_id"
        const val COLUMN_VIDEO_BLOB = "video_blob"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createVideoMetadataTableSQL = """
            CREATE TABLE $TABLE_VIDEO_METADATA (
                $COLUMN_ID TEXT PRIMARY KEY,
                $COLUMN_TITLE TEXT NOT NULL,
                $COLUMN_ORIGINAL_FILE_NAME TEXT,
                $COLUMN_DURATION_MS INTEGER,
                $COLUMN_RESOLUTION_WIDTH INTEGER,
                $COLUMN_RESOLUTION_HEIGHT INTEGER,
                $COLUMN_ORIGINAL_SIZE_BYTES INTEGER,
                $COLUMN_COMPRESSED_SIZE_BYTES INTEGER,
                $COLUMN_MIME_TYPE TEXT,
                $COLUMN_DATE_ADDED INTEGER
            )
        """.trimIndent()
        db.execSQL(createVideoMetadataTableSQL)

        val createVideoDataTableSQL = """
            CREATE TABLE $TABLE_VIDEO_DATA (
                $COLUMN_VIDEO_METADATA_ID TEXT PRIMARY KEY,
                $COLUMN_VIDEO_BLOB BLOB NOT NULL,
                FOREIGN KEY($COLUMN_VIDEO_METADATA_ID) REFERENCES $TABLE_VIDEO_METADATA($COLUMN_ID) ON DELETE CASCADE
            )
        """.trimIndent()
        db.execSQL(createVideoDataTableSQL)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_VIDEO_DATA")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_VIDEO_METADATA")
        onCreate(db)
    }

    // Basic CRUD method stubs (to be implemented more fully later)

    fun insertVideo(videoMetadata: VideoMetadata, videoData: ByteArray): Long {
        val db = this.writableDatabase
        var metadataRowId: Long = -1
        db.beginTransaction()
        try {
            val metadataValues = ContentValues().apply {
                put(COLUMN_ID, videoMetadata.id)
                put(COLUMN_TITLE, videoMetadata.title)
                put(COLUMN_ORIGINAL_FILE_NAME, videoMetadata.originalFileName)
                put(COLUMN_DURATION_MS, videoMetadata.durationMs)
                put(COLUMN_RESOLUTION_WIDTH, videoMetadata.resolutionWidth)
                put(COLUMN_RESOLUTION_HEIGHT, videoMetadata.resolutionHeight)
                put(COLUMN_ORIGINAL_SIZE_BYTES, videoMetadata.originalSizeBytes)
                put(COLUMN_COMPRESSED_SIZE_BYTES, videoMetadata.compressedSizeBytes)
                put(COLUMN_MIME_TYPE, videoMetadata.mimeType)
                put(COLUMN_DATE_ADDED, videoMetadata.dateAdded)
            }
            metadataRowId = db.insert(TABLE_VIDEO_METADATA, null, metadataValues)

            if (metadataRowId != -1L) {
                val videoDataValues = ContentValues().apply {
                    put(COLUMN_VIDEO_METADATA_ID, videoMetadata.id)
                    put(COLUMN_VIDEO_BLOB, videoData)
                }
                db.insert(TABLE_VIDEO_DATA, null, videoDataValues)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        return metadataRowId // Indicates success/failure of metadata insertion primarily
    }

    fun getVideoMetadata(id: String): VideoMetadata? {
        // Placeholder
        return null
    }

    fun getVideoData(videoMetadataId: String): ByteArray? {
        // Placeholder
        return null
    }

    fun getAllVideoMetadata(): List<VideoMetadata> {
        // Placeholder
        return emptyList()
    }

    fun deleteVideo(id: String) {
        // Placeholder
    }

    fun updateVideoMetadata(videoMetadata: VideoMetadata) {
        // Placeholder
    }
}
