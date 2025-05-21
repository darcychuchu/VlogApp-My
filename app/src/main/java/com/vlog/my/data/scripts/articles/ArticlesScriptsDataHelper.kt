package com.vlog.my.data.scripts.articles

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Environment
import java.io.File

class ArticlesScriptsDataHelper(val context: Context, databaseName: String = DATABASE_NAME, val userScriptId: String? = null) : SQLiteOpenHelper(context.applicationContext, databaseName, null, DATABASE_VERSION) {

    // 数据库文件路径
    private val dbPath = getDatabaseFilePath(context, databaseName, userScriptId)
    private var myDatabase: SQLiteDatabase? = null

    init {
        // 确保数据库文件存储在外部公共目录
        val dbFile = File(dbPath)
        if (!dbFile.parentFile?.exists()!!) {
            dbFile.parentFile?.mkdirs()
        }

        // 将数据库文件复制到外部存储
        copyDatabaseToExternalStorage()
    }

    /**
     * 将数据库文件复制到外部存储
     */
    private fun copyDatabaseToExternalStorage() {
        try {
            // 先让SQLiteOpenHelper创建内部数据库
            val db = super.getReadableDatabase()
            db.close()

            // 获取内部数据库文件
            val internalDbFile = context.getDatabasePath(databaseName)
            val externalDbFile = File(dbPath)

            // 如果外部数据库不存在，复制内部数据库到外部存储
            if (!externalDbFile.exists() && internalDbFile.exists()) {
                internalDbFile.copyTo(externalDbFile, overwrite = true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 重写获取可读数据库方法，使用外部存储的数据库文件
     */
    override fun getReadableDatabase(): SQLiteDatabase {
        if (myDatabase == null || !myDatabase!!.isOpen) {
            myDatabase = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY)
        }
        return myDatabase!!
    }

    /**
     * 重写获取可写数据库方法，使用外部存储的数据库文件
     */
    override fun getWritableDatabase(): SQLiteDatabase {
        if (myDatabase == null || !myDatabase!!.isOpen) {
            myDatabase = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE)
        }
        return myDatabase!!
    }

    /**
     * 关闭数据库
     */
    override fun close() {
        if (myDatabase != null && myDatabase!!.isOpen) {
            myDatabase!!.close()
        }
        super.close()
    }

    /**
     * 获取数据库文件的完整路径，用于显示给用户
     */
    fun getDatabasePath(): String {
        return dbPath
    }

    /**
     * 获取数据库文件所在的目录路径
     */
    fun getDatabaseDirectory(): String {
        val dbFile = File(dbPath)
        return dbFile.parentFile?.absolutePath ?: ""
    }

    companion object {
        private const val DATABASE_NAME = "sub-scripts.db"
        private const val DATABASE_VERSION = 1
        private const val DATABASE_FOLDER = "MyApplication/Databases"

        /**
         * 获取数据库文件路径
         * @param context 上下文
         * @param databaseName 数据库名称
         * @param userScriptId API配置ID，用于创建唯一的数据库目录
         * @return 数据库文件的完整路径
         */
        private fun getDatabasePath(context: Context, databaseName: String, userScriptId: String? = null): String {
            // 创建外部存储的公共目录
            val baseFolder = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                DATABASE_FOLDER
            )
            
            // 如果提供了userScriptId，则在基础目录下创建以userScriptId命名的子目录
            val folder = if (!userScriptId.isNullOrEmpty()) {
                File(baseFolder, userScriptId)
            } else {
                baseFolder
            }
            
            if (!folder.exists()) {
                folder.mkdirs()
            }
            return File(folder, databaseName).absolutePath
        }

        /**
         * 获取数据库文件的完整路径，用于显示给用户
         * @param context 上下文
         * @param databaseName 数据库名称
         * @param userScriptId API配置ID，用于创建唯一的数据库目录
         * @return 数据库文件的完整路径
         */
        fun getDatabaseFilePath(context: Context, databaseName: String = DATABASE_NAME, userScriptId: String? = null): String {
            return getDatabasePath(context, databaseName, userScriptId)
        }

        // 新增表
        private const val TABLE_ITEMS = "items"
        private const val COLUMN_ID = "id"
        private const val COLUMN_TITLE = "title"
        private const val COLUMN_PIC = "pic"
        private const val COLUMN_TAGS = "tags"
        private const val COLUMN_CONTENT = "content"
        private const val COLUMN_CATEGORY_ID = "category_id"
        private const val COLUMN_SOURCE_URL = "source_api_id"
        private const val COLUMN_SCRIPT_ID = "script_id"


        // Categories表字段
        private const val TABLE_CATEGORIES = "categories"
        private const val COLUMN_PARENT_ID = "parent_id"

    }

    override fun onCreate(db: SQLiteDatabase) {


        val createItemsTableQuery = """
            CREATE TABLE $TABLE_ITEMS (
                $COLUMN_ID TEXT PRIMARY KEY,
                $COLUMN_TITLE TEXT NOT NULL,
                $COLUMN_PIC TEXT,
                $COLUMN_CONTENT TEXT,
                $COLUMN_CATEGORY_ID TEXT,
                $COLUMN_TAGS TEXT,
                $COLUMN_SOURCE_URL TEXT,
                $COLUMN_SCRIPT_ID TEXT NOT NULL
            )
        """.trimIndent()

        val createCategoriesTableQuery = """
            CREATE TABLE $TABLE_CATEGORIES (
                $COLUMN_ID TEXT PRIMARY KEY,
                $COLUMN_TITLE TEXT NOT NULL,
                $COLUMN_PARENT_ID TEXT,
                $COLUMN_SCRIPT_ID TEXT NOT NULL
            )
        """.trimIndent()

        db.execSQL(createItemsTableQuery)
        db.execSQL(createCategoriesTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
//        if (oldVersion < 2) {
//            // 创建新表
//            val createItemsTableQuery = """
//                CREATE TABLE $TABLE_ITEMS (
//                    $COLUMN_ID TEXT NOT NULL,
//                    $COLUMN_TITLE TEXT NOT NULL,
//                    $COLUMN_PIC TEXT,
//                    $COLUMN_CONTENT TEXT,
//                    $COLUMN_CATEGORY_ID TEXT,
//                    $COLUMN_TAGS TEXT,
//                    $COLUMN_SOURCE_API_URL TEXT NOT NULL
//                )
//            """.trimIndent()
//
//            val createCategoriesTableQuery = """
//                CREATE TABLE $TABLE_CATEGORIES (
//                    $COLUMN_ID TEXT NOT NULL,
//                    $COLUMN_TITLE TEXT NOT NULL,
//                    $COLUMN_PARENT_ID TEXT,
//                    $COLUMN_CATEGORY_SOURCE_API_URL TEXT NOT NULL
//                )
//            """.trimIndent()
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
//
//            db.execSQL(createItemsTableQuery)
//            db.execSQL(createCategoriesTableQuery)
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





    // Items表操作方法
    fun insertOrUpdateItem(item: ArticlesItems): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ID, item.id)
            put(COLUMN_TITLE, item.title)
            item.pic?.let { put(COLUMN_PIC, it) }
            item.content?.let { put(COLUMN_CONTENT, it) }
            item.categoryId?.let { put(COLUMN_CATEGORY_ID, it) }
            item.tags?.let { put(COLUMN_TAGS, it) }
            item.sourceUrl?.let { put(COLUMN_SOURCE_URL, it) }
            item.scriptId?.let { put(COLUMN_SCRIPT_ID, it) }
        }
        return db.insertWithOnConflict(TABLE_ITEMS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    // Items表操作方法
    fun insertItem(item: ArticlesItems): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ID, item.id)
            put(COLUMN_TITLE, item.title)
            item.pic?.let { put(COLUMN_PIC, it) }
            item.content?.let { put(COLUMN_CONTENT, it) }
            item.categoryId?.let { put(COLUMN_CATEGORY_ID, it) }
            item.tags?.let { put(COLUMN_TAGS, it) }
            item.sourceUrl?.let { put(COLUMN_SOURCE_URL, it) }
            item.scriptId?.let { put(COLUMN_SCRIPT_ID, it) }
        }
        return db.insert(TABLE_ITEMS, null, values)
    }

    fun updateItem(item: ArticlesItems): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, item.title)
            item.pic?.let { put(COLUMN_PIC, it) }
            item.content?.let { put(COLUMN_CONTENT, it) }
            item.categoryId?.let { put(COLUMN_CATEGORY_ID, it) }
            item.tags?.let { put(COLUMN_TAGS, it) }
            item.sourceUrl?.let { put(COLUMN_SOURCE_URL, it) }
            item.scriptId?.let { put(COLUMN_SCRIPT_ID, it) }
        }
        return db.update(TABLE_ITEMS, values, "$COLUMN_ID = ?", arrayOf(item.id.toString()))
    }

    fun getItemById(id: String): ArticlesItems? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_ITEMS,
            null,
            "$COLUMN_ID = ?",
            arrayOf(id.toString()),
            null, null, null
        )

        return cursor.use {
            if (it.moveToFirst()) {
                ArticlesItems(
                    id = it.getString(it.getColumnIndexOrThrow(COLUMN_ID)),
                    title = it.getString(it.getColumnIndexOrThrow(COLUMN_TITLE)),
                    pic = it.getString(it.getColumnIndexOrThrow(COLUMN_PIC)),
                    content = it.getString(it.getColumnIndexOrThrow(COLUMN_CONTENT)),
                    categoryId = it.getString(it.getColumnIndexOrThrow(COLUMN_CATEGORY_ID)),
                    tags = it.getString(it.getColumnIndexOrThrow(COLUMN_TAGS)),
                    scriptId = it.getString(it.getColumnIndexOrThrow(COLUMN_SCRIPT_ID)),
                    sourceUrl = it.getString(it.getColumnIndexOrThrow(COLUMN_SOURCE_URL))
                )
            } else {
                null
            }
        }
    }

    fun getAllItems(): List<ArticlesItems> {
        val items = mutableListOf<ArticlesItems>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_ITEMS,
            null,
            null,
            null,
            null,
            null,
            "$COLUMN_ID DESC"
        )

        cursor.use {
            while (it.moveToNext()) {
                val item = ArticlesItems(
                    id = it.getString(it.getColumnIndexOrThrow(COLUMN_ID)),
                    title = it.getString(it.getColumnIndexOrThrow(COLUMN_TITLE)),
                    pic = it.getString(it.getColumnIndexOrThrow(COLUMN_PIC)),
                    content = it.getString(it.getColumnIndexOrThrow(COLUMN_CONTENT)),
                    categoryId = it.getString(it.getColumnIndexOrThrow(COLUMN_CATEGORY_ID)),
                    tags = it.getString(it.getColumnIndexOrThrow(COLUMN_TAGS)),
                    scriptId = it.getString(it.getColumnIndexOrThrow(COLUMN_SCRIPT_ID)),
                    sourceUrl = it.getString(it.getColumnIndexOrThrow(COLUMN_SOURCE_URL))
                )
                items.add(item)
            }
        }
        return items
    }

    fun deleteItem(id: String): Int {
        val db = writableDatabase
        return db.delete(TABLE_ITEMS, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }








    // Categories表操作方法
    fun insertCategory(category: ArticlesCategories): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            category.id?.let { put(COLUMN_ID, it) }
            put(COLUMN_TITLE, category.title)
            category.parentId?.let { put(COLUMN_PARENT_ID, it) }
            category.scriptId?.let { put(COLUMN_SCRIPT_ID, it) }
        }
        return db.insert(TABLE_CATEGORIES, null, values)
    }

    fun updateCategory(category: ArticlesCategories): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, category.title)
            category.parentId?.let { put(COLUMN_PARENT_ID, it) }
            category.scriptId?.let { put(COLUMN_SCRIPT_ID, it) }
        }
        return db.update(TABLE_CATEGORIES, values, "$COLUMN_ID = ?", arrayOf(category.id.toString()))
    }

    fun getCategoryById(id: String): ArticlesCategories? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_CATEGORIES,
            null,
            "$COLUMN_ID = ?",
            arrayOf(id.toString()),
            null, null, null
        )

        return cursor.use {
            if (it.moveToFirst()) {
                ArticlesCategories(
                    id = it.getString(it.getColumnIndexOrThrow(COLUMN_ID)),
                    title = it.getString(it.getColumnIndexOrThrow(COLUMN_TITLE)),
                    parentId = it.getString(it.getColumnIndexOrThrow(COLUMN_PARENT_ID)),
                    scriptId = it.getString(it.getColumnIndexOrThrow(COLUMN_SCRIPT_ID))
                )
            } else {
                null
            }
        }
    }

    fun getAllCategories(): List<ArticlesCategories> {
        val categories = mutableListOf<ArticlesCategories>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_CATEGORIES,
            null,
            null,
            null,
            null,
            null,
            "$COLUMN_ID ASC"
        )

        cursor.use {
            while (it.moveToNext()) {
                val category = ArticlesCategories(
                    id = it.getString(it.getColumnIndexOrThrow(COLUMN_ID)),
                    title = it.getString(it.getColumnIndexOrThrow(COLUMN_TITLE)),
                    parentId = it.getString(it.getColumnIndexOrThrow(COLUMN_PARENT_ID)),
                    scriptId = it.getString(it.getColumnIndexOrThrow(COLUMN_SCRIPT_ID))
                )
                categories.add(category)
            }
        }
        return categories
    }

    fun deleteCategory(id: String): Int {
        val db = writableDatabase
        return db.delete(TABLE_CATEGORIES, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }



}