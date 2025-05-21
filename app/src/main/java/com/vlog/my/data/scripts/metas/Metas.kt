package com.vlog.my.data.scripts.metas

/**
 * Metas 元数据模型，用于实现用户自定义配置参数
 */
data class Metas(
    var entityId: String? = null,
    var entityType: Int? = 0,
    var metaKey: String? = null,
    var metaValue: String? = null
)

//// 创建元数据表SQL
//val createMetadataTableQuery = """
//    CREATE TABLE $TABLE_METADATA (
//        id INTEGER PRIMARY KEY AUTOINCREMENT,
//        $COLUMN_ENTITY_TYPE TEXT NOT NULL,
//        $COLUMN_ENTITY_ID TEXT NOT NULL,
//        $COLUMN_META_KEY TEXT NOT NULL,
//        $COLUMN_META_VALUE TEXT,
//        UNIQUE($COLUMN_ENTITY_TYPE, $COLUMN_ENTITY_ID, $COLUMN_META_KEY)
//    )
//""".trimIndent()
//
//
//// 设置元数据
//fun setMetadata(entityType: String, entityId: String, key: String, value: String?) {
//    val db = writableDatabase
//    val values = ContentValues().apply {
//        put(COLUMN_ENTITY_TYPE, entityType)
//        put(COLUMN_ENTITY_ID, entityId)
//        put(COLUMN_META_KEY, key)
//        put(COLUMN_META_VALUE, value)
//    }
//
//    db.insertWithOnConflict(
//        TABLE_METADATA,
//        null,
//        values,
//        SQLiteDatabase.CONFLICT_REPLACE
//    )
//}
//
//// 获取元数据
//fun getMetadata(entityType: String, entityId: String, key: String): String? {
//    val db = readableDatabase
//    val cursor = db.query(
//        TABLE_METADATA,
//        arrayOf(COLUMN_META_VALUE),
//        "$COLUMN_ENTITY_TYPE = ? AND $COLUMN_ENTITY_ID = ? AND $COLUMN_META_KEY = ?",
//        arrayOf(entityType, entityId, key),
//        null, null, null
//    )
//
//    return cursor.use {
//        if (it.moveToFirst()) {
//            it.getString(0)
//        } else {
//            null
//        }
//    }
//}
//
//// 获取实体的所有元数据
//fun getAllMetadata(entityType: String, entityId: String): Map<String, String> {
//    val metadata = mutableMapOf<String, String>()
//    val db = readableDatabase
//    val cursor = db.query(
//        TABLE_METADATA,
//        arrayOf(COLUMN_META_KEY, COLUMN_META_VALUE),
//        "$COLUMN_ENTITY_TYPE = ? AND $COLUMN_ENTITY_ID = ?",
//        arrayOf(entityType, entityId),
//        null, null, null
//    )
//
//    cursor.use {
//        while (it.moveToNext()) {
//            val key = it.getString(it.getColumnIndexOrThrow(COLUMN_META_KEY))
//            val value = it.getString(it.getColumnIndexOrThrow(COLUMN_META_VALUE))
//            if (value != null) {
//                metadata[key] = value
//            }
//        }
//    }
//    return metadata
//}
//
//// 删除元数据
//fun deleteMetadata(entityType: String, entityId: String, key: String? = null): Int {
//    val db = writableDatabase
//    val whereClause = if (key != null) {
//        "$COLUMN_ENTITY_TYPE = ? AND $COLUMN_ENTITY_ID = ? AND $COLUMN_META_KEY = ?"
//    } else {
//        "$COLUMN_ENTITY_TYPE = ? AND $COLUMN_ENTITY_ID = ?"
//    }
//
//    val whereArgs = if (key != null) {
//        arrayOf(entityType, entityId, key)
//    } else {
//        arrayOf(entityType, entityId)
//    }
//
//    return db.delete(TABLE_METADATA, whereClause, whereArgs)
//}
//
//// 扩展插入项目方法，支持元数据
//fun insertItemWithMetadata(item: Items, metadata: Map<String, String>? = null): Long {
//    val db = writableDatabase
//    db.beginTransaction()
//    try {
//        // 插入基本项目数据
//        val itemId = insertItem(item)
//
//        // 如果有元数据，插入元数据
//        if (itemId > 0 && metadata != null) {
//            for ((key, value) in metadata) {
//                setMetadata("item", item.id, key, value)
//            }
//        }
//
//        db.setTransactionSuccessful()
//        return itemId
//    } finally {
//        db.endTransaction()
//    }
//}
//
//// 获取项目及其元数据
//fun getItemWithMetadata(id: String): Pair<Items?, Map<String, String>> {
//    val item = getItemById(id)
//    val metadata = if (item != null) {
//        getAllMetadata("item", id)
//    } else {
//        emptyMap()
//    }
//    return Pair(item, metadata)
//}
//
//
//
//fun importItemFromJson(jsonObject: JSONObject, apiUrl: String): Long {
//    // 解析基本字段
//    val id = jsonObject.optString("id")
//    val title = jsonObject.optString("title")
//    val pic = jsonObject.optString("pic")
//    val content = jsonObject.optString("content")
//    val categoryId = jsonObject.optString("category_id")
//    val tags = jsonObject.optString("tags")
//
//    // 创建基本项目对象
//    val item = Items(
//        id = id,
//        title = title,
//        pic = pic,
//        content = content,
//        category_id = categoryId,
//        tags = tags,
//        source_api_url = apiUrl
//    )
//
//    // 收集额外字段作为元数据
//    val metadata = mutableMapOf<String, String>()
//    val iterator = jsonObject.keys()
//    while (iterator.hasNext()) {
//        val key = iterator.next()
//        // 跳过已处理的基本字段
//        if (key !in listOf("id", "title", "pic", "content", "category_id", "tags")) {
//            val value = jsonObject.optString(key)
//            if (value.isNotEmpty()) {
//                metadata[key] = value
//            }
//        }
//    }
//
//    // 插入项目及其元数据
//    return insertItemWithMetadata(item, metadata)
//}
//
//private fun getUniqueDbName(context: Context): String {
//    val packageName = context.packageName
//    return "${packageName}_stories.db"
//}
//
//private fun getUniqueDbName(): String {
//    val uuid = UUID.randomUUID().toString()
//    return "stories_${uuid}.db"
//}
//
//private fun getUniqueDbName(context: Context): String {
//    val packageName = context.packageName
//    val versionCode = context.packageManager.getPackageInfo(packageName, 0).versionCode
//    return "${packageName}_v${versionCode}_stories.db"
//}
//
//class LocalDataHelper(
//    context: Context,
//    databaseName: String = "stories_${context.packageName}.db"
//) : SQLiteOpenHelper(context, databaseName, null, DATABASE_VERSION) {
//    // ...
//}
//
//
//class LocalDataHelper(
//    context: Context,
//    databaseName: String? = null
//) : SQLiteOpenHelper(
//    context,
//    databaseName ?: "${context.packageName}_stories.db",
//    null,
//    DATABASE_VERSION
//) {
//    companion object {
//        // 使用常量保存默认数据库版本
//        private const val DATABASE_VERSION = 3
//
//        // 其他常量保持不变
//        // ...
//    }
//
//    // 其余代码保持不变
//    // ...
//}
//
