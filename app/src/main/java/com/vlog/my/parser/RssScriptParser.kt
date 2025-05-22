package com.vlog.my.parser

import android.util.Xml
import com.vlog.my.data.scripts.SubScripts // Keep for interface consistency, though not used in parse methods
import com.vlog.my.data.scripts.articles.ArticlesCategories
import com.vlog.my.data.scripts.articles.ArticlesItems
import com.vlog.my.data.scripts.articles.ArticlesMappingConfig
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.StringReader

class RssScriptParser : ScriptParser {

    @Throws(XmlPullParserException::class, IOException::class)
    override fun parseArticlesItems(
        apiResponse: String,
        articlesMappingConfig: ArticlesMappingConfig.ItemsMapping,
        scriptId: String?
    ): List<ArticlesItems> {
        val items = mutableListOf<ArticlesItems>()
        val parser: XmlPullParser = Xml.newPullParser()
        // Process namespaces: false can simplify things if namespaces are not strictly needed for path matching.
        // If true, parser.name might include prefix, requiring careful handling.
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(StringReader(apiResponse))

        var eventType = parser.eventType
        val rootPathSegments = articlesMappingConfig.rootPath.split('.')
        val currentTagPath = mutableListOf<String>()
        var currentItemData: MutableMap<String, String?> = mutableMapOf() // Value can be null for optional fields
        var currentText: String? = null
        var insideMatchingItemElement = false

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    currentTagPath.add(parser.name)
                    currentText = null // Reset text at the start of a new tag

                    if (!insideMatchingItemElement && currentTagPath.size == rootPathSegments.size &&
                        currentTagPath.zip(rootPathSegments).all { (current, expected) -> current == expected }) {
                        insideMatchingItemElement = true
                        currentItemData = mutableMapOf()
                    } else if (insideMatchingItemElement) {
                        // Handle attributes for specific tags if mapped
                        val tagName = parser.name
                        articlesMappingConfig.picField?.let {
                            if (it.contains("@") && it.startsWith(tagName + "@")) {
                                val attributeName = it.substringAfter("@")
                                currentItemData[it] = parser.getAttributeValue(null, attributeName)
                            }
                        }
                        // Add similar blocks for other fields if they can also be attributes
                        // e.g., articlesMappingConfig.sourceUrlField if it could be an attribute
                    }
                }
                XmlPullParser.TEXT -> {
                    if (insideMatchingItemElement) {
                        // Concatenate text if it's split across multiple TEXT events (though trim usually handles this for simple cases)
                        currentText = (currentText ?: "") + parser.text?.trim()
                    }
                }
                XmlPullParser.END_TAG -> {
                    val endedTagName = parser.name
                    if (insideMatchingItemElement) {
                        // Check if we are exiting the main item element
                        if (currentTagPath.size == rootPathSegments.size && endedTagName == rootPathSegments.last() &&
                            currentTagPath.zip(rootPathSegments).all { (current, expected) -> current == expected }) {
                            val item = ArticlesItems(
                                id = currentItemData[articlesMappingConfig.idField] ?: "", // Default to empty if not found
                                title = currentItemData[articlesMappingConfig.titleField] ?: "",
                                pic = articlesMappingConfig.picField?.let { currentItemData[it] },
                                content = articlesMappingConfig.contentField?.let { currentItemData[it] },
                                categoryId = articlesMappingConfig.categoryIdField?.let { currentItemData[it] },
                                tags = articlesMappingConfig.tagsField?.let { currentItemData[it] },
                                scriptId = scriptId,
                                link = articlesMappingConfig.sourceUrlField?.let { currentItemData[it] }
                            )
                            items.add(item)
                            insideMatchingItemElement = false
                        } else if (currentTagPath.size > rootPathSegments.size) {
                            // This is a child tag within an item element
                            if (currentText != null && currentText!!.isNotEmpty()) {
                                // Store text content if the field is not an attribute-based mapping
                                if (endedTagName == articlesMappingConfig.idField && !articlesMappingConfig.idField.contains("@")) currentItemData[articlesMappingConfig.idField] = currentText
                                if (endedTagName == articlesMappingConfig.titleField && !articlesMappingConfig.titleField.contains("@")) currentItemData[articlesMappingConfig.titleField] = currentText
                                if (endedTagName == articlesMappingConfig.contentField && articlesMappingConfig.contentField?.contains("@") == false) currentItemData[articlesMappingConfig.contentField!!] = currentText
                                if (endedTagName == articlesMappingConfig.picField && articlesMappingConfig.picField?.contains("@") == false) currentItemData[articlesMappingConfig.picField!!] = currentText
                                if (endedTagName == articlesMappingConfig.sourceUrlField && articlesMappingConfig.sourceUrlField?.contains("@") == false) currentItemData[articlesMappingConfig.sourceUrlField!!] = currentText
                                if (endedTagName == articlesMappingConfig.categoryIdField && articlesMappingConfig.categoryIdField?.contains("@") == false) currentItemData[articlesMappingConfig.categoryIdField!!] = currentText
                                if (endedTagName == articlesMappingConfig.tagsField && articlesMappingConfig.tagsField?.contains("@") == false) currentItemData[articlesMappingConfig.tagsField!!] = currentText
                            }
                        }
                    }

                    if (currentTagPath.isNotEmpty() && currentTagPath.last() == endedTagName) {
                        currentTagPath.removeAt(currentTagPath.size - 1)
                    }
                    currentText = null // Reset text after processing END_TAG
                }
            }
            eventType = parser.next()
        }
        return items
    }

    override fun parseArticlesCategories(
        apiResponse: String,
        articlesMappingConfig: ArticlesMappingConfig.CategoriesMapping,
        scriptId: String?
    ): List<ArticlesCategories> {
        // For now, returning an empty list as the primary focus is items.
        // Consider implementing if XML categories are needed.
        // throw XmlPullParserException("XML Category parsing is not yet implemented for RssScriptParser.")
        return emptyList()
    }

    override fun getArticlesMappingConfigFromUserScripts(subScripts: SubScripts): ArticlesMappingConfig {
        // This method is primarily for parsers that might derive or expect configuration
        // in a specific format (like JSON) embedded within SubScripts.mappingConfig.
        // For RssScriptParser, the mapping is explicitly provided to parseArticlesItems.
        // Thus, this method might not be directly used.
        throw NotImplementedError("getArticlesMappingConfigFromUserScripts is not applicable for RssScriptParser with explicit mapping.")
    }
}

// Using XmlPullParserException and IOException directly from the method signature is standard.
// A custom exception like this might be useful if you want to wrap them with more specific context,
// but for now, it's not strictly necessary.
// class RssParseException(message: String, cause: Throwable? = null) : Exception(message, cause)
