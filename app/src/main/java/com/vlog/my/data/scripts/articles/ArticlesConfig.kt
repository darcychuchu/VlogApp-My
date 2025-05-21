package com.vlog.my.data.scripts.articles

object ArticlesConfig {

    val ITEM_CONFIG = """{
            "apiUrlField": "http://",
            "rootPath": "data.items",
            "idField": "id",
            "titleField": "title",
            "picField": "image_url",
            "contentField": "description",
            "categoryIdField": "category_id",
            "tagsField": "tags",
            "sourceUrlField": "sourceUrl",
            "urlTypeField": 0
        }""".trimIndent()

    val CATEGORY_CONFIG = """{
            "apiUrlField": "http://",
            "rootPath": "data.categories",
            "idField": "id",
            "titleField": "name",
            "parentIdField": "parent_id",
            "urlTypeField": 0
        }""".trimIndent()


    val EMPTY_CONFIG = """{}""".trimIndent()
}