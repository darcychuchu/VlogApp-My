package com.vlog.my.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.vlog.my.data.stories.Stories

class LocalDataHelper(val context: Context, databaseName: String = DATABASE_NAME) : SQLiteOpenHelper(context, databaseName, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "my-app-local-database.db"
        private const val DATABASE_VERSION = 1

        // 原有表
        private const val TABLE_STORIES = "stories"
        private const val TABLE_SETTINGS = "settings"
        private const val COLUMN_SETTING_KEY = "key"
        private const val COLUMN_SETTING_VALUE = "value"

        private const val COLUMN_ID = "id"
        private const val COLUMN_TITLE = "title"
        private const val COLUMN_TAGS = "tags"
        private const val COLUMN_CONTENT = "content"

        private const val COLUMN_CREATED_AT = "created_at"
        private const val COLUMN_IS_LOCKED = "is_locked"
        private const val COLUMN_IS_ENABLED = "is_enabled"
        private const val COLUMN_IS_TYPED = "is_typed"
        private const val COLUMN_IS_VALUED = "is_valued"
        private const val COLUMN_IS_COMMENTED = "is_commented"
        private const val COLUMN_IS_RECOMMEND = "is_recommend"
        private const val COLUMN_VERSION = "version"
        private const val COLUMN_CREATED_BY = "created_by"
        private const val COLUMN_ATTACHMENT_ID = "attachment_id"
        private const val COLUMN_DESCRIPTION = "description"
        private const val COLUMN_SHARE_CONTENT = "share_content"
        private const val COLUMN_SHARE_TYPED = "share_typed"

    }

    override fun onCreate(db: SQLiteDatabase) {
        val createStoriesTableQuery = """
            CREATE TABLE $TABLE_STORIES (
                $COLUMN_ID TEXT PRIMARY KEY,
                $COLUMN_CREATED_AT TEXT,
                $COLUMN_IS_LOCKED INTEGER,
                $COLUMN_IS_ENABLED INTEGER,
                $COLUMN_IS_TYPED INTEGER,
                $COLUMN_IS_VALUED INTEGER,
                $COLUMN_IS_COMMENTED INTEGER,
                $COLUMN_IS_RECOMMEND INTEGER,
                $COLUMN_VERSION INTEGER,
                $COLUMN_CREATED_BY TEXT,
                $COLUMN_ATTACHMENT_ID TEXT,
                $COLUMN_CONTENT TEXT,
                $COLUMN_TITLE TEXT,
                $COLUMN_DESCRIPTION TEXT,
                $COLUMN_TAGS TEXT,
                $COLUMN_SHARE_CONTENT TEXT,
                $COLUMN_SHARE_TYPED INTEGER
            )
        """.trimIndent()

        val createSettingsTableQuery = """
            CREATE TABLE $TABLE_SETTINGS (
                $COLUMN_SETTING_KEY TEXT PRIMARY KEY,
                $COLUMN_SETTING_VALUE TEXT
            )
        """.trimIndent()

        db.execSQL(createStoriesTableQuery)
        db.execSQL(createSettingsTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

    }

    // Stories配置表操作方法
    fun insertStories(story: Stories): Long {
        val db = writableDatabase

        // 先查询记录是否存在
        val cursor = db.query(
            TABLE_STORIES,
            arrayOf(COLUMN_ID),
            "$COLUMN_ID = ?",
            arrayOf(story.id),
            null, null, null
        )

        // 如果记录已存在，返回-1
        if (cursor.use { it.moveToFirst() }) {
            return -1
        }

        // 记录不存在，执行插入操作
        val values = ContentValues().apply {
            put(COLUMN_ID, story.id)
            put(COLUMN_CREATED_AT, story.createdAt)
            put(COLUMN_IS_LOCKED, story.isLocked)
            put(COLUMN_IS_ENABLED, story.isEnabled)
            put(COLUMN_IS_TYPED, story.isTyped)
            put(COLUMN_IS_VALUED, story.isValued)
            put(COLUMN_IS_COMMENTED, story.isCommented)
            put(COLUMN_IS_RECOMMEND, story.isRecommend)
            put(COLUMN_VERSION, story.version)
            put(COLUMN_CREATED_BY, story.createdBy)
            put(COLUMN_ATTACHMENT_ID, story.attachmentId)
            put(COLUMN_CONTENT, story.content)
            put(COLUMN_TITLE, story.title)
            put(COLUMN_DESCRIPTION, story.description)
            put(COLUMN_TAGS, story.tags)
            put(COLUMN_SHARE_CONTENT, story.shareContent)
            put(COLUMN_SHARE_TYPED, story.shareTyped)
        }
        return db.insert(TABLE_STORIES, null, values)
    }

    fun getSetting(key: String): String? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_SETTINGS,
            arrayOf(COLUMN_SETTING_VALUE),
            "$COLUMN_SETTING_KEY = ?",
            arrayOf(key),
            null, null, null
        )

        return cursor.use {
            if (it.moveToFirst()) {
                it.getString(0)
            } else {
                null
            }
        }
    }

    fun setSetting(key: String, value: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_SETTING_KEY, key)
            put(COLUMN_SETTING_VALUE, value)
        }

        db.insertWithOnConflict(
            TABLE_SETTINGS,
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun getAllStories(): List<Stories> {
        val stories = mutableListOf<Stories>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_STORIES,
            null,
            null,
            null,
            null,
            null,
            "$COLUMN_CREATED_AT DESC"
        )

        cursor.use {
            while (it.moveToNext()) {
                val story = Stories(
                    id = it.getString(it.getColumnIndexOrThrow(COLUMN_ID)),
                    createdAt = it.getLong(it.getColumnIndexOrThrow(COLUMN_CREATED_AT)),
                    isLocked = it.getInt(it.getColumnIndexOrThrow(COLUMN_IS_LOCKED)),
                    isEnabled = it.getInt(it.getColumnIndexOrThrow(COLUMN_IS_ENABLED)),
                    isTyped = it.getInt(it.getColumnIndexOrThrow(COLUMN_IS_TYPED)),
                    isValued = it.getInt(it.getColumnIndexOrThrow(COLUMN_IS_VALUED)),
                    isCommented = it.getInt(it.getColumnIndexOrThrow(COLUMN_IS_COMMENTED)),
                    isRecommend = it.getInt(it.getColumnIndexOrThrow(COLUMN_IS_RECOMMEND)),
                    version = it.getInt(it.getColumnIndexOrThrow(COLUMN_VERSION)),
                    createdBy = it.getString(it.getColumnIndexOrThrow(COLUMN_CREATED_BY)),
                    attachmentId = it.getString(it.getColumnIndexOrThrow(COLUMN_ATTACHMENT_ID)),
                    content = it.getString(it.getColumnIndexOrThrow(COLUMN_CONTENT)),
                    title = it.getString(it.getColumnIndexOrThrow(COLUMN_TITLE)),
                    description = it.getString(it.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                    tags = it.getString(it.getColumnIndexOrThrow(COLUMN_TAGS)),
                    shareContent = it.getString(it.getColumnIndexOrThrow(COLUMN_SHARE_CONTENT)),
                    shareTyped = it.getInt(it.getColumnIndexOrThrow(COLUMN_SHARE_TYPED))
                )
                stories.add(story)
            }
        }
        return stories
    }


}