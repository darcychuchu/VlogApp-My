package com.vlog.my.data.scripts.ebooks

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ChapterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(chapters: List<Chapter>)

    @Update
    suspend fun update(chapter: Chapter)

    @Delete
    suspend fun delete(chapter: Chapter)

    @Query("SELECT * FROM chapters WHERE ebook_id = :ebookId ORDER BY `order` ASC")
    suspend fun getChaptersForEbook(ebookId: String): List<Chapter>

    @Query("SELECT * FROM chapters WHERE id = :id")
    suspend fun getChapterById(id: String): Chapter?
}
