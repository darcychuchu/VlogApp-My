package com.vlog.my.data.bazaar

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * 小程序专用数据库
 * 与主应用的数据库分开，避免相互影响
 */
@Database(
    entities = [
        BazaarScriptsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BazaarScriptsDatabase : RoomDatabase() {
    abstract fun bazaarScriptsDao(): BazaarScriptsDao

    companion object {
        @Volatile
        private var INSTANCE: BazaarScriptsDatabase? = null

        fun getDatabase(context: Context): BazaarScriptsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BazaarScriptsDatabase::class.java,
                    "bazaar_scripts_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
