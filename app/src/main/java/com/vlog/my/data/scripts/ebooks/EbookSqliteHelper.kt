package com.vlog.my.data.scripts.ebooks

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import androidx.core.database.sqlite.transaction

class EbookSqliteHelper(context: Context, databaseName: String) :
    SQLiteOpenHelper(context, databaseName, null, DATABASE_VERSION) {

    companion object {
        private const val TAG = "EbookSqliteHelper"
        const val DATABASE_VERSION = 1

        // Ebooks Table
        const val TABLE_EBOOKS = "ebooks"
        const val COLUMN_EBOOK_ID = "id"
        const val COLUMN_EBOOK_SUBSCRIPT_ID = "sub_script_id"
        const val COLUMN_EBOOK_TITLE = "title"
        const val COLUMN_EBOOK_AUTHOR = "author"
        const val COLUMN_EBOOK_FILE_PATH = "file_path" // Original import path
        const val COLUMN_EBOOK_COVER_IMAGE_PATH = "cover_image_path"
        const val COLUMN_EBOOK_CREATED_AT = "created_at"
        const val COLUMN_EBOOK_LAST_OPENED_AT = "last_opened_at"

        // Chapters Table
        const val TABLE_CHAPTERS = "chapters"
        const val COLUMN_CHAPTER_ID = "id"
        const val COLUMN_CHAPTER_EBOOK_ID = "ebook_id"
        const val COLUMN_CHAPTER_TITLE = "title"
        const val COLUMN_CHAPTER_CONTENT = "content"
        const val COLUMN_CHAPTER_ORDER = "chapter_order" // Renamed from "order" to avoid SQL keyword conflict

        // Bookmarks Table
        const val TABLE_BOOKMARKS = "bookmarks"
        const val COLUMN_BOOKMARK_ID = "id"
        const val COLUMN_BOOKMARK_EBOOK_ID = "ebook_id"
        const val COLUMN_BOOKMARK_CHAPTER_ID = "chapter_id"
        const val COLUMN_BOOKMARK_PAGE_NUMBER = "page_number"
        const val COLUMN_BOOKMARK_PROGRESS_PERCENTAGE = "progress_percentage"
        const val COLUMN_BOOKMARK_LAST_READ_AT = "last_read_at"

        // Font Settings Table
        const val TABLE_FONT_SETTINGS = "font_settings"
        const val COLUMN_FONT_SETTING_SUBSCRIPT_ID = "sub_script_id" // This relates to the overall applet/script
        const val COLUMN_FONT_SETTING_FONT_SIZE = "font_size"

        private const val CREATE_TABLE_EBOOKS = "CREATE TABLE $TABLE_EBOOKS (" +
                "$COLUMN_EBOOK_ID TEXT PRIMARY KEY, " +
                "$COLUMN_EBOOK_SUBSCRIPT_ID TEXT NOT NULL, " +
                "$COLUMN_EBOOK_TITLE TEXT NOT NULL, " +
                "$COLUMN_EBOOK_AUTHOR TEXT, " +
                "$COLUMN_EBOOK_FILE_PATH TEXT NOT NULL, " +
                "$COLUMN_EBOOK_COVER_IMAGE_PATH TEXT, " +
                "$COLUMN_EBOOK_CREATED_AT INTEGER NOT NULL, " +
                "$COLUMN_EBOOK_LAST_OPENED_AT INTEGER)"

        private const val CREATE_TABLE_CHAPTERS = "CREATE TABLE $TABLE_CHAPTERS (" +
                "$COLUMN_CHAPTER_ID TEXT PRIMARY KEY, " +
                "$COLUMN_CHAPTER_EBOOK_ID TEXT NOT NULL, " +
                "$COLUMN_CHAPTER_TITLE TEXT NOT NULL, " +
                "$COLUMN_CHAPTER_CONTENT TEXT NOT NULL, " +
                "$COLUMN_CHAPTER_ORDER INTEGER NOT NULL, " +
                "FOREIGN KEY($COLUMN_CHAPTER_EBOOK_ID) REFERENCES $TABLE_EBOOKS($COLUMN_EBOOK_ID) ON DELETE CASCADE)"

        private const val CREATE_INDEX_CHAPTER_EBOOK_ID =
            "CREATE INDEX idx_chapter_ebook_id ON $TABLE_CHAPTERS($COLUMN_CHAPTER_EBOOK_ID)"

        private const val CREATE_TABLE_BOOKMARKS = "CREATE TABLE $TABLE_BOOKMARKS (" +
                "$COLUMN_BOOKMARK_ID TEXT PRIMARY KEY, " +
                "$COLUMN_BOOKMARK_EBOOK_ID TEXT NOT NULL, " +
                "$COLUMN_BOOKMARK_CHAPTER_ID TEXT, " +
                "$COLUMN_BOOKMARK_PAGE_NUMBER INTEGER, " +
                "$COLUMN_BOOKMARK_PROGRESS_PERCENTAGE REAL, " +
                "$COLUMN_BOOKMARK_LAST_READ_AT INTEGER NOT NULL, " +
                "FOREIGN KEY($COLUMN_BOOKMARK_EBOOK_ID) REFERENCES $TABLE_EBOOKS($COLUMN_EBOOK_ID) ON DELETE CASCADE, " +
                "FOREIGN KEY($COLUMN_BOOKMARK_CHAPTER_ID) REFERENCES $TABLE_CHAPTERS($COLUMN_CHAPTER_ID) ON DELETE SET NULL)"

        private const val CREATE_INDEX_BOOKMARK_EBOOK_ID =
            "CREATE INDEX idx_bookmark_ebook_id ON $TABLE_BOOKMARKS($COLUMN_BOOKMARK_EBOOK_ID)"

        private const val CREATE_TABLE_FONT_SETTINGS = "CREATE TABLE $TABLE_FONT_SETTINGS (" +
                "$COLUMN_FONT_SETTING_SUBSCRIPT_ID TEXT PRIMARY KEY, " +
                "$COLUMN_FONT_SETTING_FONT_SIZE INTEGER NOT NULL)"
    }

    override fun onCreate(db: SQLiteDatabase) {
        Log.d(TAG, "Creating database tables...")
        db.execSQL(CREATE_TABLE_EBOOKS)
        db.execSQL(CREATE_TABLE_CHAPTERS)
        db.execSQL(CREATE_INDEX_CHAPTER_EBOOK_ID)
        db.execSQL(CREATE_TABLE_BOOKMARKS)
        db.execSQL(CREATE_INDEX_BOOKMARK_EBOOK_ID)
        db.execSQL(CREATE_TABLE_FONT_SETTINGS)
        Log.d(TAG, "Database tables created.")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.w(TAG, "Upgrading database from version $oldVersion to $newVersion. Old data will be lost.")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_FONT_SETTINGS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_BOOKMARKS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CHAPTERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_EBOOKS")
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.w(TAG, "Downgrading database from version $oldVersion to $newVersion. Old data will be lost.")
        onUpgrade(db, oldVersion, newVersion) // Treat downgrade same as upgrade for simplicity here
    }
    
    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    // --- Ebook CRUD ---
    fun addEbook(ebook: Ebook): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_EBOOK_ID, ebook.id)
            put(COLUMN_EBOOK_SUBSCRIPT_ID, ebook.subScriptId)
            put(COLUMN_EBOOK_TITLE, ebook.title)
            put(COLUMN_EBOOK_AUTHOR, ebook.author)
            put(COLUMN_EBOOK_FILE_PATH, ebook.filePath)
            put(COLUMN_EBOOK_COVER_IMAGE_PATH, ebook.coverImagePath)
            put(COLUMN_EBOOK_CREATED_AT, ebook.createdAt)
            put(COLUMN_EBOOK_LAST_OPENED_AT, ebook.lastOpenedAt)
        }
        val result = db.insert(TABLE_EBOOKS, null, values)
        // db.close() // Not recommended to close db after each operation if helper is long-lived
        return result
    }

    fun getEbook(ebookId: String): Ebook? {
        val db = this.readableDatabase
        var ebook: Ebook? = null
        val cursor: Cursor? = db.query(
            TABLE_EBOOKS, null, "$COLUMN_EBOOK_ID = ?", arrayOf(ebookId),
            null, null, null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                ebook = Ebook(
                    id = it.getString(it.getColumnIndexOrThrow(COLUMN_EBOOK_ID)),
                    subScriptId = it.getString(it.getColumnIndexOrThrow(COLUMN_EBOOK_SUBSCRIPT_ID)),
                    title = it.getString(it.getColumnIndexOrThrow(COLUMN_EBOOK_TITLE)),
                    author = it.getStringOrNull(it.getColumnIndexOrThrow(COLUMN_EBOOK_AUTHOR)),
                    filePath = it.getString(it.getColumnIndexOrThrow(COLUMN_EBOOK_FILE_PATH)),
                    coverImagePath = it.getStringOrNull(it.getColumnIndexOrThrow(COLUMN_EBOOK_COVER_IMAGE_PATH)),
                    createdAt = it.getLong(it.getColumnIndexOrThrow(COLUMN_EBOOK_CREATED_AT)),
                    lastOpenedAt = it.getLongOrNull(it.getColumnIndexOrThrow(COLUMN_EBOOK_LAST_OPENED_AT))
                )
            }
        }
        // cursor?.close() // use {} handles close
        // db.close()
        return ebook
    }

    fun getEbooksForSubScript(subScriptId: String): List<Ebook> {
        val ebooks = mutableListOf<Ebook>()
        val db = this.readableDatabase
        val cursor: Cursor? = db.query(
            TABLE_EBOOKS, null, "$COLUMN_EBOOK_SUBSCRIPT_ID = ?", arrayOf(subScriptId),
            null, null, "$COLUMN_EBOOK_LAST_OPENED_AT DESC" // Example ordering
        )

        cursor?.use {
            while (it.moveToNext()) {
                ebooks.add(
                    Ebook(
                        id = it.getString(it.getColumnIndexOrThrow(COLUMN_EBOOK_ID)),
                        subScriptId = it.getString(it.getColumnIndexOrThrow(COLUMN_EBOOK_SUBSCRIPT_ID)),
                        title = it.getString(it.getColumnIndexOrThrow(COLUMN_EBOOK_TITLE)),
                        author = it.getStringOrNull(it.getColumnIndexOrThrow(COLUMN_EBOOK_AUTHOR)),
                        filePath = it.getString(it.getColumnIndexOrThrow(COLUMN_EBOOK_FILE_PATH)),
                        coverImagePath = it.getStringOrNull(it.getColumnIndexOrThrow(COLUMN_EBOOK_COVER_IMAGE_PATH)),
                        createdAt = it.getLong(it.getColumnIndexOrThrow(COLUMN_EBOOK_CREATED_AT)),
                        lastOpenedAt = it.getLongOrNull(it.getColumnIndexOrThrow(COLUMN_EBOOK_LAST_OPENED_AT))
                    )
                )
            }
        }
        return ebooks
    }

    fun updateEbook(ebook: Ebook): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            // Not updating ID or subScriptId
            put(COLUMN_EBOOK_TITLE, ebook.title)
            put(COLUMN_EBOOK_AUTHOR, ebook.author)
            put(COLUMN_EBOOK_FILE_PATH, ebook.filePath)
            put(COLUMN_EBOOK_COVER_IMAGE_PATH, ebook.coverImagePath)
            // createdAt should typically not be updated
            put(COLUMN_EBOOK_LAST_OPENED_AT, ebook.lastOpenedAt)
        }
        return db.update(TABLE_EBOOKS, values, "$COLUMN_EBOOK_ID = ?", arrayOf(ebook.id))
    }

    fun deleteEbook(ebookId: String): Int {
        val db = this.writableDatabase
        // Deleting an ebook will also delete associated chapters and bookmarks due to ON DELETE CASCADE
        return db.delete(TABLE_EBOOKS, "$COLUMN_EBOOK_ID = ?", arrayOf(ebookId))
    }

    // --- Chapter CRUD ---
    fun addChapter(chapter: Chapter): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_CHAPTER_ID, chapter.id)
            put(COLUMN_CHAPTER_EBOOK_ID, chapter.ebookId)
            put(COLUMN_CHAPTER_TITLE, chapter.title)
            put(COLUMN_CHAPTER_CONTENT, chapter.content)
            put(COLUMN_CHAPTER_ORDER, chapter.order)
        }
        return db.insert(TABLE_CHAPTERS, null, values)
    }

    fun addChapters(chapters: List<Chapter>) {
        val db = this.writableDatabase
        db.transaction {
            try {
                for (chapter in chapters) {
                    addChapter(chapter) // Uses the single addChapter method internally
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error adding chapters in transaction", e)
            } finally {
            }
        }
    }

    fun getChapter(chapterId: String): Chapter? {
        val db = this.readableDatabase
        var chapter: Chapter? = null
        val cursor: Cursor? = db.query(
            TABLE_CHAPTERS, null, "$COLUMN_CHAPTER_ID = ?", arrayOf(chapterId),
            null, null, null
        )
        cursor?.use {
            if (it.moveToFirst()) {
                chapter = Chapter(
                    id = it.getString(it.getColumnIndexOrThrow(COLUMN_CHAPTER_ID)),
                    ebookId = it.getString(it.getColumnIndexOrThrow(COLUMN_CHAPTER_EBOOK_ID)),
                    title = it.getString(it.getColumnIndexOrThrow(COLUMN_CHAPTER_TITLE)),
                    content = it.getString(it.getColumnIndexOrThrow(COLUMN_CHAPTER_CONTENT)),
                    order = it.getInt(it.getColumnIndexOrThrow(COLUMN_CHAPTER_ORDER))
                )
            }
        }
        return chapter
    }

    fun getChaptersForEbook(ebookId: String): List<Chapter> {
        val chapters = mutableListOf<Chapter>()
        val db = this.readableDatabase
        val cursor: Cursor? = db.query(
            TABLE_CHAPTERS, null, "$COLUMN_CHAPTER_EBOOK_ID = ?", arrayOf(ebookId),
            null, null, "$COLUMN_CHAPTER_ORDER ASC"
        )
        cursor?.use {
            while (it.moveToNext()) {
                chapters.add(
                    Chapter(
                        id = it.getString(it.getColumnIndexOrThrow(COLUMN_CHAPTER_ID)),
                        ebookId = it.getString(it.getColumnIndexOrThrow(COLUMN_CHAPTER_EBOOK_ID)),
                        title = it.getString(it.getColumnIndexOrThrow(COLUMN_CHAPTER_TITLE)),
                        content = it.getString(it.getColumnIndexOrThrow(COLUMN_CHAPTER_CONTENT)),
                        order = it.getInt(it.getColumnIndexOrThrow(COLUMN_CHAPTER_ORDER))
                    )
                )
            }
        }
        return chapters
    }

    fun updateChapter(chapter: Chapter): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_CHAPTER_TITLE, chapter.title)
            put(COLUMN_CHAPTER_CONTENT, chapter.content)
            put(COLUMN_CHAPTER_ORDER, chapter.order)
            // ebookId and id are not updated
        }
        return db.update(TABLE_CHAPTERS, values, "$COLUMN_CHAPTER_ID = ?", arrayOf(chapter.id))
    }

    fun deleteChapter(chapterId: String): Int {
        val db = this.writableDatabase
        return db.delete(TABLE_CHAPTERS, "$COLUMN_CHAPTER_ID = ?", arrayOf(chapterId))
    }

    fun deleteChaptersForEbook(ebookId: String): Int {
        val db = this.writableDatabase
        return db.delete(TABLE_CHAPTERS, "$COLUMN_CHAPTER_EBOOK_ID = ?", arrayOf(ebookId))
    }

    // --- Bookmark CRUD ---
    fun addBookmark(bookmark: Bookmark): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_BOOKMARK_ID, bookmark.id)
            put(COLUMN_BOOKMARK_EBOOK_ID, bookmark.ebookId)
            put(COLUMN_BOOKMARK_CHAPTER_ID, bookmark.chapterId)
            put(COLUMN_BOOKMARK_PAGE_NUMBER, bookmark.pageNumber)
            put(COLUMN_BOOKMARK_PROGRESS_PERCENTAGE, bookmark.progressPercentage)
            put(COLUMN_BOOKMARK_LAST_READ_AT, bookmark.lastReadAt)
        }
        return db.insert(TABLE_BOOKMARKS, null, values)
    }

    fun getBookmark(bookmarkId: String): Bookmark? {
        val db = this.readableDatabase
        var bookmark: Bookmark? = null
        val cursor: Cursor? = db.query(
            TABLE_BOOKMARKS, null, "$COLUMN_BOOKMARK_ID = ?", arrayOf(bookmarkId),
            null, null, null
        )
        cursor?.use {
            if (it.moveToFirst()) {
                bookmark = Bookmark(
                    id = it.getString(it.getColumnIndexOrThrow(COLUMN_BOOKMARK_ID)),
                    ebookId = it.getString(it.getColumnIndexOrThrow(COLUMN_BOOKMARK_EBOOK_ID)),
                    chapterId = it.getStringOrNull(it.getColumnIndexOrThrow(COLUMN_BOOKMARK_CHAPTER_ID)),
                    pageNumber = it.getIntOrNull(it.getColumnIndexOrThrow(COLUMN_BOOKMARK_PAGE_NUMBER)),
                    progressPercentage = it.getFloatOrNull(it.getColumnIndexOrThrow(COLUMN_BOOKMARK_PROGRESS_PERCENTAGE)),
                    lastReadAt = it.getLong(it.getColumnIndexOrThrow(COLUMN_BOOKMARK_LAST_READ_AT))
                )
            }
        }
        return bookmark
    }

    fun getBookmarkForEbook(ebookId: String): Bookmark? {
        val db = this.readableDatabase
        var bookmark: Bookmark? = null
        // Assuming one bookmark per ebook for simplicity, or the most recent one if multiple allowed
        val cursor: Cursor? = db.query(
            TABLE_BOOKMARKS, null, "$COLUMN_BOOKMARK_EBOOK_ID = ?", arrayOf(ebookId),
            null, null, "$COLUMN_BOOKMARK_LAST_READ_AT DESC", "1"
        )
        cursor?.use {
            if (it.moveToFirst()) {
                bookmark = Bookmark(
                    id = it.getString(it.getColumnIndexOrThrow(COLUMN_BOOKMARK_ID)),
                    ebookId = it.getString(it.getColumnIndexOrThrow(COLUMN_BOOKMARK_EBOOK_ID)),
                    chapterId = it.getStringOrNull(it.getColumnIndexOrThrow(COLUMN_BOOKMARK_CHAPTER_ID)),
                    pageNumber = it.getIntOrNull(it.getColumnIndexOrThrow(COLUMN_BOOKMARK_PAGE_NUMBER)),
                    progressPercentage = it.getFloatOrNull(it.getColumnIndexOrThrow(COLUMN_BOOKMARK_PROGRESS_PERCENTAGE)),
                    lastReadAt = it.getLong(it.getColumnIndexOrThrow(COLUMN_BOOKMARK_LAST_READ_AT))
                )
            }
        }
        return bookmark
    }

    fun updateBookmark(bookmark: Bookmark): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_BOOKMARK_CHAPTER_ID, bookmark.chapterId)
            put(COLUMN_BOOKMARK_PAGE_NUMBER, bookmark.pageNumber)
            put(COLUMN_BOOKMARK_PROGRESS_PERCENTAGE, bookmark.progressPercentage)
            put(COLUMN_BOOKMARK_LAST_READ_AT, bookmark.lastReadAt)
            // ebookId and id are not updated
        }
        return db.update(TABLE_BOOKMARKS, values, "$COLUMN_BOOKMARK_ID = ?", arrayOf(bookmark.id))
    }

    fun deleteBookmark(bookmarkId: String): Int {
        val db = this.writableDatabase
        return db.delete(TABLE_BOOKMARKS, "$COLUMN_BOOKMARK_ID = ?", arrayOf(bookmarkId))
    }
    
    fun deleteBookmarkForEbook(ebookId: String): Int {
        val db = this.writableDatabase
        return db.delete(TABLE_BOOKMARKS, "$COLUMN_BOOKMARK_EBOOK_ID = ?", arrayOf(ebookId))
    }

    // --- FontSetting CRUD ---
    fun addOrUpdateFontSetting(fontSetting: FontSetting): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_FONT_SETTING_SUBSCRIPT_ID, fontSetting.subScriptId)
            put(COLUMN_FONT_SETTING_FONT_SIZE, fontSetting.fontSize)
        }
        // Use insertWithOnConflict with CONFLICT_REPLACE for upsert behavior
        return db.insertWithOnConflict(TABLE_FONT_SETTINGS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getFontSetting(subScriptId: String): FontSetting? {
        val db = this.readableDatabase
        var fontSetting: FontSetting? = null
        val cursor: Cursor? = db.query(
            TABLE_FONT_SETTINGS, null, "$COLUMN_FONT_SETTING_SUBSCRIPT_ID = ?", arrayOf(subScriptId),
            null, null, null
        )
        cursor?.use {
            if (it.moveToFirst()) {
                fontSetting = FontSetting(
                    subScriptId = it.getString(it.getColumnIndexOrThrow(COLUMN_FONT_SETTING_SUBSCRIPT_ID)),
                    fontSize = it.getInt(it.getColumnIndexOrThrow(COLUMN_FONT_SETTING_FONT_SIZE))
                )
            }
        }
        return fontSetting
    }

    fun deleteFontSetting(subScriptId: String): Int {
        val db = this.writableDatabase
        return db.delete(TABLE_FONT_SETTINGS, "$COLUMN_FONT_SETTING_SUBSCRIPT_ID = ?", arrayOf(subScriptId))
    }
}

// Helper extension for Cursor to get String or Null
fun Cursor.getStringOrNull(columnIndex: Int): String? {
    return if (this.isNull(columnIndex)) null else this.getString(columnIndex)
}

// Helper extension for Cursor to get Long or Null
fun Cursor.getLongOrNull(columnIndex: Int): Long? {
    return if (this.isNull(columnIndex)) null else this.getLong(columnIndex)
}

// Helper extension for Cursor to get Int or Null
fun Cursor.getIntOrNull(columnIndex: Int): Int? {
    return if (this.isNull(columnIndex)) null else this.getInt(columnIndex)
}

// Helper extension for Cursor to get Float or Null
fun Cursor.getFloatOrNull(columnIndex: Int): Float? {
    return if (this.isNull(columnIndex)) null else this.getFloat(columnIndex)
}
