package com.vlog.my.data.scripts.configs

/**
 * FieldsConfig 字段映射
 * @param idField 返回数据的 ID 字段映射
 * @param titleField 返回数据的 标题 字段映射
 * @param picField 返回数据的 图片 字段映射
 * @param contentField 返回数据的 内容 字段映射
 * @param tagsField 返回数据的 关键字 字段映射
 * @param sourceUrlField 返回数据的 链接地址 字段映射
 * @param metaList 返回数据的 自定义字段映射
 */
data class FieldsConfig(
    val fieldId: String,
    val quoteId: String,
    val idField: String,
    val titleField: String,
    val picField: String? = null,
    val contentField: String? = null,
    val tagsField: String? = null,
    val sourceUrlField: String? = null,
    val metaList: MutableList<MetasConfig>? = null
)