package com.vlog.my.data.scripts.ebooks

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.vlog.my.data.scripts.EbookScripts

@Entity(tableName = "ebooks")
data class Ebook(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "sub_script_id") val subScriptId: String, // Added index = true here for clarity, though covered by table indices
    val title: String,
    val author: String?,
    @ColumnInfo(name = "file_path") val filePath: String,
    @ColumnInfo(name = "cover_image_path") val coverImagePath: String?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "last_opened_at") val lastOpenedAt: Long?
)
