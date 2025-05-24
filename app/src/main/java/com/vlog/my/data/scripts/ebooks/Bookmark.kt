package com.vlog.my.data.scripts.ebooks

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class Bookmark(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "ebook_id") val ebookId: String, // Added index = true here for clarity
    @ColumnInfo(name = "chapter_id") val chapterId: String?, // Added index = true here for clarity
    @ColumnInfo(name = "page_number") val pageNumber: Int?,
    @ColumnInfo(name = "progress_percentage") val progressPercentage: Float?,
    @ColumnInfo(name = "last_read_at") val lastReadAt: Long
)
