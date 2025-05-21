package com.vlog.my.data.scripts

import kotlin.enums.EnumEntries

/**
 * 内容类型枚举
 * 定义支持的内容类型
 */
enum class ContentType(val typeId: Int, val typeName: String) {
    BROWSER(0,"浏览器"),
    NEWS(1,"新闻"),
    MUSIC(2,"音乐"),
    MOVIE(3,"电影"),
    EBOOK(4,"电子书");

    companion object {
        fun toList(): EnumEntries<ContentType> {
            return entries
        }

        fun findById(idKey: Int): ContentType? {
            for (typeEnum in entries) {
                if (typeEnum.typeId == idKey) {
                    return typeEnum
                }
            }
            return null
        }
    }


}