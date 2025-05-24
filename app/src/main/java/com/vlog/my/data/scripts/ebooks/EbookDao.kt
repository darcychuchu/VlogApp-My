package com.vlog.my.data.scripts.ebooks

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface EbookDao {
    @Insert
    suspend fun insert(ebook: Ebook)

    @Update
    suspend fun update(ebook: Ebook)

    @Delete
    suspend fun delete(ebook: Ebook)

    @Query("SELECT * FROM ebooks WHERE id = :id")
    suspend fun getEbookById(id: String): Ebook?

    @Query("SELECT * FROM ebooks WHERE sub_script_id = :subScriptId")
    suspend fun getEbooksForSubScript(subScriptId: String): List<Ebook>

    @Query("SELECT * FROM ebooks")
    suspend fun getAllEbooks(): List<Ebook>
}
