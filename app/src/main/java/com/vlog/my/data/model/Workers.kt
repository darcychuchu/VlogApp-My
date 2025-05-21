package com.vlog.my.data.model

data class Workers(
    val name: String,
    var api: String? = null,
    var version: Int? = null,
    var isEnabled: Int = 0,
    var isValued: Int = 15,
    var refreshCount: Int = 0,
    var lastRefreshTime: Long = 0,
    var updatedAt: Long = System.currentTimeMillis(),
    val pageField: String? = null,
    var pageValued: Int = 0,
    val paramsField: String? = null,
    var paramsValued: String? = null,
    val typeField: String? = null,
    var typeValued: String? = null
)