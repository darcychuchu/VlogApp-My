package com.vlog.my.parser

import com.vlog.my.data.scripts.SubScripts
import com.vlog.my.data.scripts.articles.ArticlesCategories
import com.vlog.my.data.scripts.articles.ArticlesItems
import com.vlog.my.data.scripts.articles.ArticlesMappingConfig
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * JSON API解析器实现类，用于解析JSON格式的API响应
 */
class ArticlesScriptParser : ScriptParser {
    
    override fun parseArticlesItems(apiResponse: String, articlesMappingConfig: ArticlesMappingConfig.ItemsMapping, scriptId: String?): List<ArticlesItems> {
        val items = mutableListOf<ArticlesItems>()
        try {
            val jsonObject = JSONObject(apiResponse)

            // 获取根路径的数据
            val paths = articlesMappingConfig.rootPath.split(".")
            var currentJson: Any = jsonObject

            // 逐层解析路径
            for (path in paths) {
                currentJson = when (currentJson) {
                    is JSONObject -> currentJson.get(path)
                    else -> throw JSONException("无法解析路径: ${articlesMappingConfig.rootPath}")
                }
            }
            
            // 处理数组数据
            val itemsArray = when (currentJson) {
                is JSONArray -> currentJson
                is JSONObject -> JSONArray().put(currentJson)
                else -> throw JSONException("无法解析为数组或对象: $currentJson")
            }
            
            // 遍历数组，解析每个item
            for (i in 0 until itemsArray.length()) {
                val itemJson = itemsArray.getJSONObject(i)
                val item = ArticlesItems(
                    id = getStringValue(itemJson, articlesMappingConfig.idField),
                    title = getStringValue(itemJson, articlesMappingConfig.titleField),
                    pic = articlesMappingConfig.picField?.let { getStringValue(itemJson, it) },
                    content = articlesMappingConfig.contentField?.let { getStringValue(itemJson, it) },
                    categoryId = articlesMappingConfig.categoryIdField?.let { getStringValue(itemJson, it) },
                    tags = articlesMappingConfig.tagsField?.let { getStringValue(itemJson, it) },
                    scriptId = scriptId
                )
                items.add(item)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return items
    }
    
    override fun parseArticlesCategories(apiResponse: String, articlesMappingConfig: ArticlesMappingConfig.CategoriesMapping, scriptId: String?): List<ArticlesCategories> {
        val categories = mutableListOf<ArticlesCategories>()
        try {
            val jsonObject = JSONObject(apiResponse)
            
            // 获取根路径的数据
            val paths = articlesMappingConfig.rootPath.split(".")
            var currentJson: Any = jsonObject
            
            // 逐层解析路径
            for (path in paths) {
                currentJson = when (currentJson) {
                    is JSONObject -> currentJson.get(path)
                    else -> throw JSONException("无法解析路径: ${articlesMappingConfig.rootPath}")
                }
            }
            
            // 处理数组数据
            val categoriesArray = when (currentJson) {
                is JSONArray -> currentJson
                is JSONObject -> JSONArray().put(currentJson)
                else -> throw JSONException("无法解析为数组或对象: $currentJson")
            }
            
            // 遍历数组，解析每个category
            for (i in 0 until categoriesArray.length()) {
                val categoryJson = categoriesArray.getJSONObject(i)
                val category = ArticlesCategories(
                    id = getStringValue(categoryJson, articlesMappingConfig.idField),
                    title = getStringValue(categoryJson, articlesMappingConfig.titleField),
                    parentId = articlesMappingConfig.parentIdField?.let { getStringValue(categoryJson, it) },
                    scriptId = scriptId
                )
                categories.add(category)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return categories
    }

    override fun getArticlesMappingConfigFromUserScripts(subScripts: SubScripts): ArticlesMappingConfig {
        return try {
            val jsonObject = JSONObject(subScripts.mappingConfig)

            val itemsState = jsonObject.getInt("itemsState") == 1
            val categoriesState = jsonObject.getInt("categoriesState") == 1


            // 解析items映射配置
            val itemsMapping = if (itemsState && jsonObject.has("itemsMapping")) {
                val itemsJson = jsonObject.getJSONObject("itemsMapping")
                ArticlesMappingConfig.ItemsMapping(
                    rootPath = itemsJson.getString("rootPath"),
                    idField = itemsJson.getString("idField"),
                    titleField = itemsJson.getString("titleField"),
                    picField = if (itemsJson.has("picField")) itemsJson.getString("picField") else null,
                    contentField = if (itemsJson.has("contentField")) itemsJson.getString("contentField") else null,
                    categoryIdField = if (itemsJson.has("categoryIdField")) itemsJson.getString("categoryIdField") else null,
                    tagsField = if (itemsJson.has("tagsField")) itemsJson.getString("tagsField") else null,
                    urlTypeField = itemsJson.getInt("urlTypeField"),
                    apiUrlField = itemsJson.getString("apiUrlField")
                )
            } else null
            
            // 解析categories映射配置
            val categoriesMapping = if (categoriesState && jsonObject.has("categoriesMapping")) {
                val categoriesJson = jsonObject.getJSONObject("categoriesMapping")
                ArticlesMappingConfig.CategoriesMapping(
                    rootPath = categoriesJson.getString("rootPath"),
                    idField = categoriesJson.getString("idField"),
                    titleField = categoriesJson.getString("titleField"),
                    parentIdField = if (categoriesJson.has("parentIdField")) categoriesJson.getString("parentIdField") else null,
                    urlTypeField = categoriesJson.getInt("urlTypeField"),
                    apiUrlField = categoriesJson.getString("apiUrlField")
                )
            } else null

            ArticlesMappingConfig(0,0,itemsMapping,categoriesMapping)
        } catch (e: Exception) {
            e.printStackTrace()
            ArticlesMappingConfig(0,0,null, null)
        }
    }
    
    /**
     * 从JSON对象中获取字符串值，支持嵌套路径（如"user.name"）
     */
    private fun getStringValue(jsonObject: JSONObject, path: String): String {
        val paths = path.split(".")
        var currentJson: JSONObject = jsonObject
        
        // 处理嵌套路径
        for (i in 0 until paths.size - 1) {
            currentJson = currentJson.optJSONObject(paths[i]) ?: return ""
        }
        
        return currentJson.optString(paths.last(), "")
    }
    
    /**
     * 从JSON对象中获取整数值，支持嵌套路径
     */
    private fun getIntValue(jsonObject: JSONObject, path: String): Int? {
        val paths = path.split(".")
        var currentJson: JSONObject = jsonObject
        
        // 处理嵌套路径
        for (i in 0 until paths.size - 1) {
            currentJson = currentJson.optJSONObject(paths[i]) ?: return null
        }
        
        return try {
            if (currentJson.has(paths.last())) currentJson.getInt(paths.last()) else null
        } catch (e: Exception) {
            null
        }
    }
}