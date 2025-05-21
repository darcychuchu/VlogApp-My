package com.vlog.my.data.scripts.articles

data class ArticlesMappingConfig(
    val itemsState: Int = 0,
    val categoriesState: Int = 0,
    var itemsMapping: ItemsMapping? = null,
    var categoriesMapping: CategoriesMapping? = null
) {
    /**
     * items表字段映射
     * @param rootPath API响应中items数据的根路径，例如"data.items"
     * @param titleField 标题字段映射
     * @param picField 图片字段映射
     * @param contentField 内容字段映射
     * @param categoryIdField 分类ID字段映射
     * @param tagsField 标签字段映射
     */
    data class ItemsMapping(
        val rootPath: String,
        val idField: String,
        val titleField: String,
        val picField: String? = null,
        val contentField: String? = null,
        val categoryIdField: String? = null,
        val tagsField: String? = null,
        val sourceUrlField: String? = null,
        val urlTypeField: Int = 0, // 指定使用的URL类型,
        val apiUrlField: String // 指定使用的API URL类型
    )

    /**
     * categories表字段映射
     * @param rootPath API响应中categories数据的根路径，例如"data.categories"
     * @param titleField 标题字段映射
     * @param parentIdField 父分类ID字段映射
     */
    data class CategoriesMapping(
        val rootPath: String,
        val idField: String,
        val titleField: String,
        val parentIdField: String? = null,
        val urlTypeField: Int = 0, // 指定使用的URL类型,
        val apiUrlField: String // 指定使用的API URL类型
    )
}