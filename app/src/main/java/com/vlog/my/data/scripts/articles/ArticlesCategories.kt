package com.vlog.my.data.scripts.articles

/**
 * 分类数据模型，对应数据库中的categories表
 */
data class ArticlesCategories(
    var id: String? = null,
    var title: String = "",
    var parentId: String? = null,
    var scriptId: String? = null  // 脚本 ID
)