package com.vlog.my.data.scripts.articles

/**
 * 通用项目数据模型，对应数据库中的items表
 */
data class ArticlesItems(
    var id: String? = null,
    var title: String = "",
    var pic: String? = null,
    var content: String? = null,
    var categoryId: String? = null,
    var tags: String? = null,
    var sourceUrl: String? = null,  // 数据来源的
    var scriptId: String? = null    // 脚本 ID
)