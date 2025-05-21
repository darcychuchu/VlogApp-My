package com.vlog.my.data

data class ApiResponse<T>(
    val code: Int = 200,  // 默认值设为200
    val message: String? = null,
    val data: T? = null
)

data class PaginatedResponse<T>(
    val items: List<T>?,
    val total: Int = 0,
    val page: Int = 1,
    val pageSize: Int = 10
)