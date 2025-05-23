package com.vlog.my.data.scripts.ebooks

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "chapters",
    foreignKeys = [
        ForeignKey(
            entity = Ebook::class,
            parentColumns = ["id"],
            childColumns = ["ebook_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["ebook_id"]), Index(value = ["order"])]
)
data class Chapter(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "ebook_id", index = true) val ebookId: String, // Added index = true here for clarity
    val title: String,
    @ColumnInfo(name = "content") val content: String, // Changed from contentFilePath to content
    @ColumnInfo(name = "order") val order: Int // Explicitly added @ColumnInfo for clarity
)
