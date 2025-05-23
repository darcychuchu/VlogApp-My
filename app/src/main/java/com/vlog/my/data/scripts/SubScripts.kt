package com.vlog.my.data.scripts

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * API配置数据模型，用于存储用户配置的API信息
 */
@Entity(tableName = "sub_scripts") // Added @Entity annotation
data class SubScripts(
    @PrimaryKey var id: String, // Made id non-nullable and PrimaryKey
    var name: String = "",         // API名称
    var url: String? = null,      // 列表数据API URL
    var listUrl: String? = null,      // 列表数据API URL
    var logoUrl: String? = null,      // logo URL
    var apiKey: String? = null,     // API密钥
    var mappingConfig: String = "", // 映射配置（JSON格式）
    var databaseName: String? = null,  // 关联的数据库名称
    var isTyped: Int = 0,  // 0 = article|news，1= music|audio ， 2 = movie|video ，3 = ebook
    var isEnabled: Int = 0,  // 0 = local,    1= online
    var isLocked: Int = 0,  // 0 = free,    1=contribute & isValued = 10
    var isValued: Int = 0,  // isLocked = 1, isValued = contribute & bonus
    var version: Int = 0,
    var createdBy: String? = null
    // Note: The commented out Json fields are not relevant for Room unless you use a TypeConverter for them.
) {
    // Add a no-argument constructor for Room
    constructor() : this(id = "", name = "")
}