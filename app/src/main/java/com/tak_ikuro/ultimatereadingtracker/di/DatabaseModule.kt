package com.tak_ikuro.ultimatereadingtracker.di

import android.content.Context
import androidx.room.Room
import com.tak_ikuro.ultimatereadingtracker.data.local.database.AppDatabase
import com.tak_ikuro.ultimatereadingtracker.data.local.database.dao.BookDao
import com.tak_ikuro.ultimatereadingtracker.data.local.database.dao.InsightDao
import com.tak_ikuro.ultimatereadingtracker.data.local.database.dao.ReadingMotivationDao
import com.tak_ikuro.ultimatereadingtracker.data.local.database.dao.ReviewDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }
    
    @Provides
    @Singleton
    fun provideBookDao(database: AppDatabase): BookDao {
        return database.bookDao()
    }
    
    @Provides
    @Singleton
    fun provideReviewDao(database: AppDatabase): ReviewDao {
        return database.reviewDao()
    }
    
    @Provides
    @Singleton
    fun provideInsightDao(database: AppDatabase): InsightDao {
        return database.insightDao()
    }
    
    @Provides
    @Singleton
    fun provideReadingMotivationDao(database: AppDatabase): ReadingMotivationDao {
        return database.readingMotivationDao()
    }
}