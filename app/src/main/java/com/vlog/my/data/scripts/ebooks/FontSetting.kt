package com.vlog.my.data.scripts.ebooks

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.vlog.my.data.scripts.EbookScripts

@Entity(tableName = "font_settings")
data class FontSetting(
    @PrimaryKey @ColumnInfo(name = "sub_script_id") val subScriptId: String,
    @ColumnInfo(name = "font_size") val fontSize: Int
)
