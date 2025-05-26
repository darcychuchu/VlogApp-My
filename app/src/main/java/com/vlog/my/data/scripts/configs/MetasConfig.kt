package com.vlog.my.data.scripts.configs

/**
 * MetasConfig 自定义映射
 * @param metaId 自定义映射 ID
 * @param metaTyped 自定义映射 类型
 * @param metaKey 返回数据的 字段名称
 * @param metaValue 解析 字段映射 名称
 * @param metaList 解析 字段映射 列表
 */
data class MetasConfig(
    val metaId: String,
    val quoteId: String,
    val metaTyped: Int? = 0,
    val metaKey: String? = null,
    val metaValue: String? = null,
    val metaList: MutableList<MetasConfig>? = null
)