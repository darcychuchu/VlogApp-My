package com.vlog.my.data.scripts.ebooks

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.vlog.my.data.scripts.SubScripts // Import the actual SubScripts class

@Entity(
    tableName = "font_settings",
    foreignKeys = [
        ForeignKey(
            entity = SubScripts::class,
            parentColumns = ["id"],
            childColumns = ["sub_script_id"],
            onDelete = ForeignKey.CASCADE
            // deferred = true // Consider using deferred if SubScripts table might not exist yet
        )
    ],
    indices = [Index(value = ["sub_script_id"], unique = true)] // sub_script_id should be unique for font settings
)
data class FontSetting(
    @PrimaryKey @ColumnInfo(name = "sub_script_id") val subScriptId: String,
    @ColumnInfo(name = "font_size") val fontSize: Int
)
