package com.vlog.my.data.stories

import com.vlog.my.data.model.Attachments
import com.vlog.my.data.users.Users

data class Stories(
    var id: String? = null,
    var createdAt: Long? = null,
    var isLocked: Int? = null,  // 重要字段
    var isEnabled: Int? = null,
    var isTyped: Int? = null,      // 主要字段 0=动态（图文 / stories），1=作品（视频 / artworks）
    var isValued: Int? = null,     // 重要字段，和 isLocked 搭配使用
    var isCommented: Int? = null,
    var isRecommend: Int? = null,
    var orderSort: Int? = null,
    var version: Int? = null,
    var createdBy: String? = null,  // 主要字段 存储Users 的name字段
    var forwardedBy: String? = null,
    var attachmentId: String? = null, // 存储attachmentId 通过 IMAGE_BASE_URL 展示作品artworks封面，或者图文stories的列表图
    var content: String? = null,   // 引用小程序关键字段
    var shareContent: String? = null, // 用于存储分享内容的JSON
    var shareTyped: Int? = null,     // 分享内容类型：0=未知，1=视频，2=电子书
    var title: String? = null,      // 可为空
    var description: String? = null, // 不能为空
    var tags: String? = null,       // 可为空

    var createdByItem: Users? = null,
    var attachmentItem: Attachments? = null,
    var attachmentList: List<Attachments>? = null
)
