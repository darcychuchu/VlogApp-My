package com.vlog.my.data.bazaar

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BazaarScriptsDao {
    @Query("SELECT * FROM bazaar_scripts")
    fun findAllBazaarScripts(): Flow<List<BazaarScriptsEntity>>
    
    @Query("SELECT * FROM bazaar_scripts WHERE id = :id")
    suspend fun getBazaarScriptsById(id: String): BazaarScriptsEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBazaarScripts(bazaarScript: BazaarScriptsEntity)
    
    @Update
    suspend fun updateBazaarScripts(bazaarScript: BazaarScriptsEntity)
    
    @Delete
    suspend fun deleteBazaarScripts(bazaarScript: BazaarScriptsEntity)
}
