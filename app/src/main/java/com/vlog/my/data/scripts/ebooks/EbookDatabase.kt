package com.vlog.my.data.scripts.ebooks

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.vlog.my.data.scripts.EbookScripts

@Database(
    entities = [Ebook::class, Chapter::class, Bookmark::class, FontSetting::class, EbookScripts::class], // Added SubScripts::class
    version = 1,
    exportSchema = false // Or true if you want to export schema
)
abstract class EbookDatabase : RoomDatabase() {

    abstract fun ebookDao(): EbookDao
    abstract fun chapterDao(): ChapterDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun fontSettingDao(): FontSettingDao
    // abstract fun subScriptsDao(): SubScriptsDao // If you create a DAO for SubScripts

    companion object {
        @Volatile
        private var INSTANCE: EbookDatabase? = null

        // Store instances in a map to support multiple databases
        private val instances = mutableMapOf<String, EbookDatabase>()

        fun getInstance(context: Context, dbName: String): EbookDatabase {
            return instances[dbName] ?: synchronized(this) {
                instances[dbName] ?: buildDatabase(context.applicationContext, dbName)
                    .also { instances[dbName] = it }
            }
        }
        
        private fun buildDatabase(context: Context, dbName: String): EbookDatabase {
            return Room.databaseBuilder(
                context,
                EbookDatabase::class.java,
                dbName
            )
            // Add migrations if necessary, or fallbackToDestructiveMigration
            .fallbackToDestructiveMigration() // Use this only for development, implement proper migrations for production
            .build()
        }

        // Optional: Method to close and remove an instance if no longer needed
        fun closeInstance(dbName: String) {
            synchronized(this) {
                instances.remove(dbName)?.close()
            }
        }
    }
}
