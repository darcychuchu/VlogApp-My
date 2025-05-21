package com.vlog.my.data.model

data class Attachments(
    var id: String? = null,
    var createdAt: Long? = null,
    var isTyped: Int? = null,  // 9 = avatar，8= artworks cover ， 7 = Stories Images ，0 = file 下载文件
    var version: Int? = null,
    var createdBy: String? = null, // 主要字段 存储Users 的name字段
    var quoteId: String? = null, // 主要字段 存储Stories 的 id 字段
    var size: Long? = null
)
