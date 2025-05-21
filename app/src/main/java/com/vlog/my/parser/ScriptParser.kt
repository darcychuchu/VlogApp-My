package com.vlog.my.parser

import com.vlog.my.data.scripts.SubScripts
import com.vlog.my.data.scripts.articles.ArticlesCategories
import com.vlog.my.data.scripts.articles.ArticlesItems
import com.vlog.my.data.scripts.articles.ArticlesMappingConfig

/**
 * API解析器接口，定义解析API数据的方法
 */
interface ScriptParser {
    /**
     * 解析API响应数据，并返回解析后的items列表
     * @param apiResponse API响应数据（JSON字符串）
     * @param itemsMappingConfig 映射配置
     * @param scriptId 数据来源的API Url
     * @return 解析后的items列表
     */
    fun parseArticlesItems(apiResponse: String, itemsMappingConfig: ArticlesMappingConfig.ItemsMapping, scriptId: String? = null): List<ArticlesItems>
    
    /**
     * 解析API响应数据，并返回解析后的categories列表
     * @param apiResponse API响应数据（JSON字符串）
     * @param categoriesMappingConfig 映射配置
     * @param scriptId 数据来源的API Url
     * @return 解析后的categories列表
     */
    fun parseArticlesCategories(apiResponse: String, categoriesMappingConfig: ArticlesMappingConfig.CategoriesMapping, scriptId: String? = null): List<ArticlesCategories>
    
    /**
     * 从API配置中获取映射配置
     * @param subScripts API配置
     * @return 映射配置
     */
    fun getArticlesMappingConfigFromUserScripts(subScripts: SubScripts): ArticlesMappingConfig
}