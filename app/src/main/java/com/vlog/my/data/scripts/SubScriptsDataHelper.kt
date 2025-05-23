package com.vlog.my.data.scripts

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.vlog.my.data.model.Workers
import java.util.UUID
import androidx.core.database.sqlite.transaction

class SubScriptsDataHelper(val context: Context, databaseName: String = DATABASE_NAME) : SQLiteOpenHelper(context, databaseName, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "sub-scripts-database.db"
        private const val DATABASE_VERSION = 1

        // 原有表
        private const val COLUMN_ID = "id"
        private const val COLUMN_IS_LOCKED = "is_locked"
        private const val COLUMN_IS_ENABLED = "is_enabled"
        private const val COLUMN_IS_TYPED = "is_typed"
        private const val COLUMN_IS_VALUED = "is_valued"
        private const val COLUMN_VERSION = "version"
        private const val COLUMN_CREATED_BY = "created_by"
        private const val COLUMN_SCRIPT_PASSWORD_HASH = "script_password_hash" // New column

        // SubScripts表字段
        private const val TABLE_SUB_SCRIPTS = "sub_scripts"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_URL = "url"
        private const val COLUMN_LIST_URL = "list_url"
        private const val COLUMN_LOGO_URL = "logo_url"
        private const val COLUMN_API_KEY = "api_key"
        private const val COLUMN_MAPPING_CONFIG = "mapping_config"
        private const val COLUMN_DATABASE_NAME = "database_name"

        // workers表字段
        private const val TABLE_WORKERS = "workers"
        private const val COLUMN_REFRESH_COUNT = "refreshCount"
        private const val COLUMN_LAST_REFRESH_TIME = "lastRefreshTime"
        private const val COLUMN_API = "api"
        private const val COLUMN_UPDATED_AT = "updatedAt"
        private const val COLUMN_PAGE_FIELD = "pageField"
        private const val COLUMN_PAGE_VALUED = "pageValued"
        private const val COLUMN_PARAMS_FIELD = "paramsField"
        private const val COLUMN_PARAMS_VALUED = "paramsValued"
        private const val COLUMN_TYPE_FIELD = "typeField"
        private const val COLUMN_TYPE_VALUED = "typeValued"

    }

    override fun onCreate(db: SQLiteDatabase) {

        val createSubScriptsTableQuery = """
            CREATE TABLE $TABLE_SUB_SCRIPTS (
                $COLUMN_ID TEXT PRIMARY KEY,
                $COLUMN_NAME TEXT NOT NULL,
                $COLUMN_URL TEXT,
                $COLUMN_LIST_URL TEXT,
                $COLUMN_LOGO_URL TEXT,
                $COLUMN_API_KEY TEXT NOT NULL,
                $COLUMN_MAPPING_CONFIG TEXT NOT NULL,
                $COLUMN_DATABASE_NAME TEXT,
                $COLUMN_IS_LOCKED INTEGER,
                $COLUMN_IS_ENABLED INTEGER,
                $COLUMN_IS_TYPED INTEGER,
                $COLUMN_IS_VALUED INTEGER,
                $COLUMN_VERSION INTEGER,
                $COLUMN_CREATED_BY TEXT,
                $COLUMN_SCRIPT_PASSWORD_HASH TEXT 
            )
        """.trimIndent()


        val createWorkersTableQuery = """
            CREATE TABLE $TABLE_WORKERS (
                $COLUMN_NAME TEXT PRIMARY KEY,
                $COLUMN_API TEXT,
                $COLUMN_VERSION INTEGER,
                $COLUMN_IS_ENABLED INTEGER,
                $COLUMN_IS_VALUED INTEGER,
                $COLUMN_REFRESH_COUNT INTEGER DEFAULT 0,
                $COLUMN_LAST_REFRESH_TIME INTEGER,
                $COLUMN_UPDATED_AT INTEGER,
                $COLUMN_PAGE_FIELD TEXT,
                $COLUMN_PAGE_VALUED  INTEGER DEFAULT 0,
                $COLUMN_PARAMS_FIELD TEXT,
                $COLUMN_PARAMS_VALUED TEXT,
                $COLUMN_TYPE_FIELD TEXT,
                $COLUMN_TYPE_VALUED TEXT
            )
        """.trimIndent()

        db.execSQL(createSubScriptsTableQuery)
        db.execSQL(createWorkersTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) { // Assuming current version is 1, next is 2
            try {
                db.execSQL("ALTER TABLE $TABLE_SUB_SCRIPTS ADD COLUMN $COLUMN_SCRIPT_PASSWORD_HASH TEXT")
            } catch (e: Exception) {
                Log.e("SubScriptsDataHelper", "Error upgrading database to add password hash column", e)
                // If the column already exists, SQLite might throw an error, which can be ignored if that's the desired outcome.
                // However, a more robust migration checks if the column exists first.
            }
        }
//        if (oldVersion < 2) {
//
//            val createApiConfigsTableQuery = """
//                CREATE TABLE $TABLE_API_CONFIGS (
//                    $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
//                    $COLUMN_NAME TEXT NOT NULL,
//                    $COLUMN_URL TEXT NOT NULL,
//                    $COLUMN_LIST_URL TEXT NOT NULL,
//                    $COLUMN_CATE_URL TEXT,
//                    $COLUMN_API_KEY TEXT,
//                    $COLUMN_MAPPING_CONFIG TEXT NOT NULL
//                )
//            """.trimIndent()
//            db.execSQL(createApiConfigsTableQuery)
//        }
//
//        if (oldVersion < 4) {
//            // 添加数据库名称字段到API配置表
//            try {
//                db.execSQL("ALTER TABLE $TABLE_API_CONFIGS ADD COLUMN $COLUMN_DATABASE_NAME TEXT")
//            } catch (e: Exception) {
//                // 如果列已存在，忽略错误
//                e.printStackTrace()
//            }
//        }
    }



    // SubScripts配置表操作方法
    fun insertUserScripts(userScriptRow: SubScripts): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ID, UUID.randomUUID().toString())
            put(COLUMN_NAME, userScriptRow.name)
            userScriptRow.url?.let { put(COLUMN_URL, it) }
            userScriptRow.listUrl?.let { put(COLUMN_LIST_URL, it) }
            userScriptRow.logoUrl?.let { put(COLUMN_LOGO_URL, it) }
            put(COLUMN_API_KEY, userScriptRow.apiKey)
            put(COLUMN_MAPPING_CONFIG, userScriptRow.mappingConfig)
            userScriptRow.databaseName?.let { put(COLUMN_DATABASE_NAME, it) }
            put(COLUMN_IS_TYPED, userScriptRow.isTyped)
            put(COLUMN_IS_LOCKED, userScriptRow.isLocked)
            put(COLUMN_IS_ENABLED, userScriptRow.isEnabled)
            put(COLUMN_IS_TYPED, userScriptRow.isTyped)
            put(COLUMN_IS_VALUED, userScriptRow.isValued)
            put(COLUMN_VERSION, userScriptRow.version)
            put(COLUMN_CREATED_BY, userScriptRow.createdBy)
            userScriptRow.scriptPasswordHash?.let { put(COLUMN_SCRIPT_PASSWORD_HASH, it) }
        }
        return db.insert(TABLE_SUB_SCRIPTS, null, values)
    }
    
    fun updateUserScripts(userScriptRow: SubScripts): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, userScriptRow.name)
            userScriptRow.url?.let { put(COLUMN_URL, it) }
            userScriptRow.listUrl?.let { put(COLUMN_LIST_URL, it) }
            userScriptRow.logoUrl?.let { put(COLUMN_LOGO_URL, it) }
            put(COLUMN_API_KEY, userScriptRow.apiKey)
            put(COLUMN_MAPPING_CONFIG, userScriptRow.mappingConfig)
            userScriptRow.databaseName?.let { put(COLUMN_DATABASE_NAME, it) }
            put(COLUMN_IS_TYPED, userScriptRow.isTyped)
            put(COLUMN_IS_LOCKED, userScriptRow.isLocked)
            put(COLUMN_IS_ENABLED, userScriptRow.isEnabled)
            // put(COLUMN_IS_TYPED, userScriptRow.isTyped) // Duplicate, removed
            put(COLUMN_IS_VALUED, userScriptRow.isValued)
            put(COLUMN_VERSION, userScriptRow.version)
            put(COLUMN_CREATED_BY, userScriptRow.createdBy)
            // Handle password hash update:
            // If it's explicitly set to null (to remove password), we need to allow that.
            // If it's not null, put it. If it's null in userScriptRow but we don't want to clear it on every update,
            // then this logic needs to be more specific, perhaps in a dedicated update method for password.
            // For now, this will update it if present or clear it if null in userScriptRow.
            if (userScriptRow.scriptPasswordHash == null) {
                putNull(COLUMN_SCRIPT_PASSWORD_HASH)
            } else {
                put(COLUMN_SCRIPT_PASSWORD_HASH, userScriptRow.scriptPasswordHash)
            }
        }
        return db.update(TABLE_SUB_SCRIPTS, values, "$COLUMN_ID = ?", arrayOf(userScriptRow.id.toString()))
    }
    
    fun getUserScriptsById(id: String): SubScripts? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_SUB_SCRIPTS,
            null,
            "$COLUMN_ID = ?",
            arrayOf(id.toString()),
            null, null, null, null
        )
        
        return cursor.use {
            if (it.moveToFirst()) {
                SubScripts(
                    id = it.getString(it.getColumnIndexOrThrow(COLUMN_ID)),
                    name = it.getString(it.getColumnIndexOrThrow(COLUMN_NAME)),
                    url = it.getString(it.getColumnIndexOrThrow(COLUMN_URL)),
                    listUrl = it.getString(it.getColumnIndexOrThrow(COLUMN_LIST_URL)),
                    logoUrl = it.getString(it.getColumnIndexOrThrow(COLUMN_LOGO_URL)),
                    apiKey = it.getString(it.getColumnIndexOrThrow(COLUMN_API_KEY)),
                    mappingConfig = it.getString(it.getColumnIndexOrThrow(COLUMN_MAPPING_CONFIG)),
                    databaseName = if (it.getColumnIndex("database_name") != -1) it.getString(it.getColumnIndexOrThrow("database_name")) else null,
                    isLocked = it.getInt(it.getColumnIndexOrThrow(COLUMN_IS_LOCKED)),
                    isTyped = it.getInt(it.getColumnIndexOrThrow(COLUMN_IS_TYPED)),
                    isEnabled = it.getInt(it.getColumnIndexOrThrow(COLUMN_IS_ENABLED)),
                    isValued = it.getInt(it.getColumnIndexOrThrow(COLUMN_IS_VALUED)),
                    version = it.getInt(it.getColumnIndexOrThrow(COLUMN_VERSION)),
                    createdBy = it.getString(it.getColumnIndexOrThrow(COLUMN_CREATED_BY)),
                    scriptPasswordHash = it.getString(it.getColumnIndexOrThrow(COLUMN_SCRIPT_PASSWORD_HASH))
                )
            } else {
                null
            }
        }
    }
    
    fun getAllUserScripts(): List<SubScripts> {
        val userScriptList = mutableListOf<SubScripts>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_SUB_SCRIPTS,
            null,
            null,
            null,
            null,
            null,
            "$COLUMN_ID ASC"
        )
        
        cursor.use {
            while (it.moveToNext()) {
                val userScriptRow = SubScripts(
                    id = it.getString(it.getColumnIndexOrThrow(COLUMN_ID)),
                    name = it.getString(it.getColumnIndexOrThrow(COLUMN_NAME)),
                    url = it.getString(it.getColumnIndexOrThrow(COLUMN_URL)),
                    listUrl = it.getString(it.getColumnIndexOrThrow(COLUMN_LIST_URL)),
                    logoUrl = it.getString(it.getColumnIndexOrThrow(COLUMN_LOGO_URL)),
                    apiKey = it.getString(it.getColumnIndexOrThrow(COLUMN_API_KEY)),
                    mappingConfig = it.getString(it.getColumnIndexOrThrow(COLUMN_MAPPING_CONFIG)),
                    databaseName = if (it.getColumnIndex("database_name") != -1) it.getString(it.getColumnIndexOrThrow("database_name")) else null,
                    isTyped = it.getInt(it.getColumnIndexOrThrow(COLUMN_IS_TYPED)),
                    isLocked = it.getInt(it.getColumnIndexOrThrow(COLUMN_IS_LOCKED)),
                    isEnabled = it.getInt(it.getColumnIndexOrThrow(COLUMN_IS_ENABLED)),
                    isValued = it.getInt(it.getColumnIndexOrThrow(COLUMN_IS_VALUED)),
                    version = it.getInt(it.getColumnIndexOrThrow(COLUMN_VERSION)),
                    createdBy = it.getString(it.getColumnIndexOrThrow(COLUMN_CREATED_BY)),
                    scriptPasswordHash = it.getString(it.getColumnIndexOrThrow(COLUMN_SCRIPT_PASSWORD_HASH))
                )
                userScriptList.add(userScriptRow)
            }
        }
        return userScriptList
    }
    
    fun deleteUserScripts(id: String): Int {
        val db = writableDatabase
        return db.delete(TABLE_SUB_SCRIPTS, "$COLUMN_ID = ?", arrayOf(id))
    }
    
    /**
     * 更新UserScripts配置的Logo URL
     * @param id API配置ID
     * @param logoUrl 新的Logo URL
     * @return 更新的行数
     */
    fun updateUserScriptsForLogo(id: String, logoUrl: String): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_LOGO_URL, logoUrl)
        }
        return db.update(TABLE_SUB_SCRIPTS, values, "$COLUMN_ID = ?", arrayOf(id))
    }

    fun updateScriptPasswordHash(scriptId: String, passwordHash: String?): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            if (passwordHash == null) {
                putNull(COLUMN_SCRIPT_PASSWORD_HASH)
            } else {
                put(COLUMN_SCRIPT_PASSWORD_HASH, passwordHash)
            }
        }
        return db.update(TABLE_SUB_SCRIPTS, values, "$COLUMN_ID = ?", arrayOf(scriptId))
    }

    fun getScriptPasswordHash(scriptId: String): String? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_SUB_SCRIPTS,
            arrayOf(COLUMN_SCRIPT_PASSWORD_HASH), // Select only the password hash column
            "$COLUMN_ID = ?",
            arrayOf(scriptId),
            null, null, null, null
        )

        return cursor.use {
            if (it.moveToFirst()) {
                it.getString(it.getColumnIndexOrThrow(COLUMN_SCRIPT_PASSWORD_HASH))
            } else {
                null
            }
        }
    }












    // Workers配置表操作方法
    fun insertWorkers(workers: Workers): Long {
        val values = ContentValues().apply {
            put(COLUMN_NAME, workers.name)
            put(COLUMN_API, workers.api)
            put(COLUMN_VERSION, workers.version)
            put(COLUMN_IS_ENABLED, workers.isEnabled)
            put(COLUMN_IS_VALUED, workers.isValued)
            put(COLUMN_REFRESH_COUNT, workers.refreshCount)
            put(COLUMN_LAST_REFRESH_TIME, workers.lastRefreshTime)
            put(COLUMN_UPDATED_AT, workers.updatedAt)
            put(COLUMN_PAGE_FIELD, workers.pageField)
            put(COLUMN_PAGE_VALUED, workers.pageValued)
            put(COLUMN_PARAMS_FIELD, workers.paramsField)
            put(COLUMN_PARAMS_VALUED, workers.paramsValued)
            put(COLUMN_TYPE_FIELD, workers.typeField)
            put(COLUMN_TYPE_VALUED, workers.typeValued)
        }
        return writableDatabase.insertWithOnConflict(
            TABLE_WORKERS,
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun getWorkers(name: String): Workers? {
        val worker = readableDatabase.query(
            TABLE_WORKERS,
            null,
            "$COLUMN_NAME = ?",
            arrayOf(name),
            null,
            null,
            null
        )

        return worker.use {
            if (it.moveToFirst()) {
                Workers(
                    name = it.getString(it.getColumnIndexOrThrow(COLUMN_NAME)),
                    api = it.getString(it.getColumnIndexOrThrow(COLUMN_API)),
                    version = it.getInt(it.getColumnIndexOrThrow(COLUMN_VERSION)),
                    isEnabled = it.getInt(it.getColumnIndexOrThrow(COLUMN_IS_ENABLED)),
                    isValued = it.getInt(it.getColumnIndexOrThrow(COLUMN_IS_VALUED)),
                    refreshCount = it.getInt(it.getColumnIndexOrThrow(COLUMN_REFRESH_COUNT)),
                    lastRefreshTime = it.getLong(it.getColumnIndexOrThrow(COLUMN_LAST_REFRESH_TIME)),
                    updatedAt = it.getLong(it.getColumnIndexOrThrow(COLUMN_UPDATED_AT)),
                    pageField = it.getString(it.getColumnIndexOrThrow(COLUMN_PAGE_FIELD)),
                    pageValued = it.getInt(it.getColumnIndexOrThrow(COLUMN_PAGE_VALUED)),
                    paramsField = it.getString(it.getColumnIndexOrThrow(COLUMN_PARAMS_FIELD)),
                    paramsValued = it.getString(it.getColumnIndexOrThrow(COLUMN_PARAMS_VALUED)),
                    typeField = it.getString(it.getColumnIndexOrThrow(COLUMN_TYPE_FIELD)),
                    typeValued = it.getString(it.getColumnIndexOrThrow(COLUMN_TYPE_VALUED))
                )
            } else {
                null
            }
        }
    }


    fun getAllWorkers(): List<Workers> {
        val workerList = mutableListOf<Workers>()
        val cursor = readableDatabase.query(
            TABLE_WORKERS,
            null,
            null,
            null,
            null,
            null,
            "$COLUMN_UPDATED_AT DESC"
        )

        cursor.use {
            while (it.moveToNext()) {
                val worker = Workers(
                    name = it.getString(it.getColumnIndexOrThrow(COLUMN_NAME)),
                    api = it.getString(it.getColumnIndexOrThrow(COLUMN_API)),
                    version = it.getInt(it.getColumnIndexOrThrow(COLUMN_VERSION)),
                    isEnabled = it.getInt(it.getColumnIndexOrThrow(COLUMN_IS_ENABLED)),
                    isValued = it.getInt(it.getColumnIndexOrThrow(COLUMN_IS_VALUED)),
                    refreshCount = it.getInt(it.getColumnIndexOrThrow(COLUMN_REFRESH_COUNT)),
                    lastRefreshTime = it.getLong(it.getColumnIndexOrThrow(COLUMN_LAST_REFRESH_TIME)),
                    updatedAt = it.getLong(it.getColumnIndexOrThrow(COLUMN_UPDATED_AT)),
                    pageField = it.getString(it.getColumnIndexOrThrow(COLUMN_PAGE_FIELD)),
                    pageValued = it.getInt(it.getColumnIndexOrThrow(COLUMN_PAGE_VALUED)),
                    paramsField = it.getString(it.getColumnIndexOrThrow(COLUMN_PARAMS_FIELD)),
                    paramsValued = it.getString(it.getColumnIndexOrThrow(COLUMN_PARAMS_VALUED)),
                    typeField = it.getString(it.getColumnIndexOrThrow(COLUMN_TYPE_FIELD)),
                    typeValued = it.getString(it.getColumnIndexOrThrow(COLUMN_TYPE_VALUED))
                )
                workerList.add(worker)
            }
        }
        return workerList
    }

    fun getWorkersByName(name: String): Workers? {
        return getWorkers(name)
    }

    fun updateWorkers(workers: Workers) {
        val values = ContentValues().apply {
            put(COLUMN_NAME, workers.name)
            put(COLUMN_API, workers.api)
            put(COLUMN_VERSION, workers.version)
            put(COLUMN_IS_ENABLED, workers.isEnabled)
            put(COLUMN_IS_VALUED, workers.isValued)
            put(COLUMN_REFRESH_COUNT, workers.refreshCount)
            put(COLUMN_LAST_REFRESH_TIME, workers.lastRefreshTime)
            put(COLUMN_UPDATED_AT, System.currentTimeMillis())
            put(COLUMN_PAGE_FIELD, workers.pageField)
            put(COLUMN_PAGE_VALUED, workers.pageValued)
            put(COLUMN_PARAMS_FIELD, workers.paramsField)
            put(COLUMN_PARAMS_VALUED, workers.paramsValued)
            put(COLUMN_TYPE_FIELD, workers.typeField)
            put(COLUMN_TYPE_VALUED, workers.typeValued)
        }

        writableDatabase.insertWithOnConflict(
            TABLE_WORKERS,
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun updateRefreshCount(name: String) {
        val workers = getWorkersByName(name)
        workers?.let {
            it.refreshCount = it.refreshCount + 1
            it.lastRefreshTime = System.currentTimeMillis()
            updateWorkers(it)
        }
    }
    
    fun batchInsertWorkers(workerList: List<Workers>) {
        writableDatabase.transaction {
            // try { // Not strictly needed if individual operations handle their own exceptions
                workerList.forEach { worker ->
                    val values = ContentValues().apply {
                        put(COLUMN_NAME, worker.name)
                        put(COLUMN_API, worker.api)
                        put(COLUMN_VERSION, worker.version)
                        put(COLUMN_IS_ENABLED, worker.isEnabled)
                        put(COLUMN_IS_VALUED, worker.isValued)
                        put(COLUMN_REFRESH_COUNT, worker.refreshCount)
                        put(COLUMN_LAST_REFRESH_TIME, worker.lastRefreshTime)
                        put(COLUMN_UPDATED_AT, worker.updatedAt)
                        put(COLUMN_PAGE_FIELD, worker.pageField)
                        put(COLUMN_PAGE_VALUED, worker.pageValued)
                        put(COLUMN_PARAMS_FIELD, worker.paramsField)
                        put(COLUMN_PARAMS_VALUED, worker.paramsValued)
                        put(COLUMN_TYPE_FIELD, worker.typeField)
                        put(COLUMN_TYPE_VALUED, worker.typeValued)
                    }
                    // Using insertWithOnConflict for workers table specifically.
                    // This part is unrelated to SubScripts password hash.
                    // The try-catch and logging here seem fine for worker batch insertion.
                    try {
                        insertWithOnConflict(
                            TABLE_WORKERS,
                            null,
                            values,
                            SQLiteDatabase.CONFLICT_REPLACE
                        )
                    }  catch (e: Exception) {
                         Log.e("SubScriptsDataHelper", "Error inserting worker ${worker.name} in batch: ${e.message}")
                    }
                }
            // } catch (e: Exception) {
            //     Log.e("DatabaseHelper", "Error in batch insert: ${e.message}")
            // } finally {
            //     Log.e("DatabaseHelper", "Access in batch insert") // This log seems out of place for a finally block of a transaction
            // }
        }
    }

}