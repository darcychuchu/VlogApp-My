package com.vlog.my.data.scripts

/**
 * API配置数据模型，用于存储用户配置的API信息
 */
//@JsonClass(generateAdapter = true)
data class SubScripts(
    var id: String? = null,
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
    var createdBy: String? = null,
    var scriptPasswordHash: String? = null // Stores salt:hash for password protection

)