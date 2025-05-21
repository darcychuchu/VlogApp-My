package com.vlog.my.data.bazaar

import kotlin.Int

data class BazaarScriptsDto(
    var id: String? = null,
    var createdAt: Long? = null,
    var isLocked: Int? = null,
    var isEnabled: Int? = null,
    var isTyped: Int? = null,
    var isValued: Int? = null,
    var isCommented: Int? = null,
    var isRecommend: Int? = null,
    var version: Int? = null,
    var createdBy: String? = null,
    var attachmentId: String? = null,
    var title: String? = null,
    var description: String? = null,
    var tags: String? = null,
    var configTyped: Int? = null,
    var configs: String? = null
)