package com.vlog.my.data.scripts.ebooks

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "bookmarks",
    foreignKeys = [
        ForeignKey(
            entity = Ebook::class,
            parentColumns = ["id"],
            childColumns = ["ebook_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Chapter::class,
            parentColumns = ["id"],
            childColumns = ["chapter_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["ebook_id"]), Index(value = ["chapter_id"])]
)
data class Bookmark(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "ebook_id", index = true) val ebookId: String, // Added index = true here for clarity
    @ColumnInfo(name = "chapter_id", index = true) val chapterId: String?, // Added index = true here for clarity
    @ColumnInfo(name = "page_number") val pageNumber: Int?,
    @ColumnInfo(name = "progress_percentage") val progressPercentage: Float?,
    @ColumnInfo(name = "last_read_at") val lastReadAt: Long
)
