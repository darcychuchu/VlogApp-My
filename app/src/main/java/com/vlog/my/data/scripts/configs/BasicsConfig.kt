package com.vlog.my.data.scripts.configs

/**
 * BasicsConfig 基础参数配置
 * @param apiUrlField 请求的API URL
 * @param urlParamsField 请求的API URL参数 : ?key=xxx | &api_key=xxx
 * @param urlTypedField 请求的API URL类型 : 默认 0 = JSON
 * @param rootPath 请求的API URL响应中数据的根路径，例如"data.list"
 * @param metaList 请求的API URL的其他配置 ： 因为api请求有很多种类，保留这个自定义接口
 * @param fieldsConfig 请求的API URL响应中数据的字段映射配置
 */
data class BasicsConfig(
    val basicId: String,
    val scriptsId: String,
    val apiUrlField: String,
    val urlParamsField: String? = null,
    val urlTypedField: Int = 0,
    val rootPath: String,
    val metaList: MutableList<MetasConfig>? = null,
    val fieldsConfig: FieldsConfig? = null,
)