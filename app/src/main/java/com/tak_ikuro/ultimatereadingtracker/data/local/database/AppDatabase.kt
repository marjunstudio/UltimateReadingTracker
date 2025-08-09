package com.tak_ikuro.ultimatereadingtracker.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.tak_ikuro.ultimatereadingtracker.data.local.database.converter.Converters
import com.tak_ikuro.ultimatereadingtracker.data.local.database.dao.BookDao
import com.tak_ikuro.ultimatereadingtracker.data.local.database.dao.InsightDao
import com.tak_ikuro.ultimatereadingtracker.data.local.database.dao.ReadingMotivationDao
import com.tak_ikuro.ultimatereadingtracker.data.local.database.dao.ReviewDao
import com.tak_ikuro.ultimatereadingtracker.data.local.database.entity.BookEntity
import com.tak_ikuro.ultimatereadingtracker.data.local.database.entity.InsightEntity
import com.tak_ikuro.ultimatereadingtracker.data.local.database.entity.ReadingMotivationEntity
import com.tak_ikuro.ultimatereadingtracker.data.local.database.entity.ReviewEntity

@Database(
    entities = [
        BookEntity::class,
        ReviewEntity::class,
        InsightEntity::class,
        ReadingMotivationEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun bookDao(): BookDao
    abstract fun reviewDao(): ReviewDao
    abstract fun insightDao(): InsightDao
    abstract fun readingMotivationDao(): ReadingMotivationDao
    
    companion object {
        const val DATABASE_NAME = "ultimate_reading_tracker_db"
    }
}