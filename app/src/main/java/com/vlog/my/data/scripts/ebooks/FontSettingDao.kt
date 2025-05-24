package com.vlog.my.data.scripts.ebooks

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FontSettingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(fontSetting: FontSetting)

    @Query("SELECT * FROM font_settings WHERE sub_script_id = :subScriptId")
    suspend fun getFontSetting(subScriptId: String): FontSetting?
}
