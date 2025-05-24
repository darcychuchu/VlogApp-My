package com.vlog.my.data.scripts.music

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MusicScriptDataHelper(context: Context, databaseName: String) :
    SQLiteOpenHelper(context, databaseName, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_VERSION = 1
        const val TABLE_MUSIC_TRACKS = "music_tracks"
        const val COLUMN_ID = "id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_ARTIST = "artist"
        const val COLUMN_ALBUM = "album"
        const val COLUMN_FILE_PATH = "file_path"
        const val COLUMN_URL = "url"
        const val COLUMN_MUSIC_DATA = "music_data"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_MUSIC_TRACKS (
                $COLUMN_ID TEXT PRIMARY KEY,
                $COLUMN_TITLE TEXT NOT NULL,
                $COLUMN_ARTIST TEXT,
                $COLUMN_ALBUM TEXT,
                $COLUMN_FILE_PATH TEXT,
                $COLUMN_URL TEXT,
                $COLUMN_MUSIC_DATA BLOB
            )
        """.trimIndent()
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Basic upgrade logic: drop table and recreate
        db.execSQL("DROP TABLE IF EXISTS $TABLE_MUSIC_TRACKS")
        onCreate(db)
    }

    fun addMusicTrack(musicItem: MusicItem): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ID, musicItem.id)
            put(COLUMN_TITLE, musicItem.title)
            put(COLUMN_ARTIST, musicItem.artist)
            put(COLUMN_ALBUM, musicItem.album)
            put(COLUMN_FILE_PATH, musicItem.filePath)
            put(COLUMN_URL, musicItem.url)
            put(COLUMN_MUSIC_DATA, musicItem.musicData)
        }
        val id = db.insert(TABLE_MUSIC_TRACKS, null, values)
        db.close()
        return id
    }

    fun getMusicTrack(id: String): MusicItem? {
        val db = this.readableDatabase
        val cursor: Cursor? = db.query(
            TABLE_MUSIC_TRACKS, null, "$COLUMN_ID = ?", arrayOf(id),
            null, null, null
        )

        var musicItem: MusicItem? = null
        cursor?.use {
            if (it.moveToFirst()) {
                musicItem = cursorToMusicItem(it)
            }
        }
        db.close()
        return musicItem
    }

    fun getAllMusicTracks(): List<MusicItem> {
        val musicTracks = mutableListOf<MusicItem>()
        val db = this.readableDatabase
        val cursor: Cursor? = db.rawQuery("SELECT * FROM $TABLE_MUSIC_TRACKS", null)

        cursor?.use {
            if (it.moveToFirst()) {
                do {
                    musicTracks.add(cursorToMusicItem(it))
                } while (it.moveToNext())
            }
        }
        db.close()
        return musicTracks
    }

    fun updateMusicTrack(musicItem: MusicItem): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, musicItem.title)
            put(COLUMN_ARTIST, musicItem.artist)
            put(COLUMN_ALBUM, musicItem.album)
            put(COLUMN_FILE_PATH, musicItem.filePath)
            put(COLUMN_URL, musicItem.url)
            put(COLUMN_MUSIC_DATA, musicItem.musicData)
        }
        val rowsAffected = db.update(
            TABLE_MUSIC_TRACKS, values, "$COLUMN_ID = ?",
            arrayOf(musicItem.id)
        )
        db.close()
        return rowsAffected
    }

    fun deleteMusicTrack(id: String): Int {
        val db = this.writableDatabase
        val rowsAffected = db.delete(TABLE_MUSIC_TRACKS, "$COLUMN_ID = ?", arrayOf(id))
        db.close()
        return rowsAffected
    }
    
    private fun cursorToMusicItem(cursor: Cursor): MusicItem {
        return MusicItem(
            id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID)),
            title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
            artist = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ARTIST)),
            album = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ALBUM)),
            filePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FILE_PATH)),
            url = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_URL)),
            musicData = cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_MUSIC_DATA))
        )
    }
}
