package com.vlog.my.data.bazaar

/**
 * API配置数据模型，用于存储用户配置的API信息
 */
data class BazaarScripts(
    var id: String? = null,
    var createdAt: Long? = null,
    var isLocked: Int? = null,
    var isEnabled: Int? = null,
    var isTyped: Int? = null,
    var isValued: Int? = null,
    var isCommented: Int? = null,
    var isRecommend: Int? = null,
    var version: Int = 0,
    var createdBy: String? = null,
    var attachmentId: String? = null,
    var title: String? = null,
    var description: String? = null,
    var tags: String? = null,
    var configTyped: Int? = null,
    var configs: String? = null
)