package com.tak_ikuro.ultimatereadingtracker.data.local.database.dao

import androidx.room.*
import com.tak_ikuro.ultimatereadingtracker.data.local.database.entity.Importance
import com.tak_ikuro.ultimatereadingtracker.data.local.database.entity.InsightEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InsightDao {
    
    @Query("SELECT * FROM insights WHERE book_id = :bookId ORDER BY importance DESC, created_at DESC")
    fun getInsightsByBookId(bookId: Long): Flow<List<InsightEntity>>
    
    @Query("SELECT * FROM insights WHERE book_id = :bookId AND importance = :importance ORDER BY created_at DESC")
    fun getInsightsByBookIdAndImportance(bookId: Long, importance: Importance): Flow<List<InsightEntity>>
    
    @Query("SELECT * FROM insights WHERE tags LIKE '%' || :tag || '%' ORDER BY importance DESC, created_at DESC")
    fun searchInsightsByTag(tag: String): Flow<List<InsightEntity>>
    
    @Query("SELECT * FROM insights WHERE id = :insightId")
    suspend fun getInsightById(insightId: Long): InsightEntity?
    
    @Query("SELECT * FROM insights ORDER BY importance DESC, created_at DESC")
    fun getAllInsights(): Flow<List<InsightEntity>>
    
    @Query("SELECT * FROM insights WHERE importance = :importance ORDER BY created_at DESC")
    fun getInsightsByImportance(importance: Importance): Flow<List<InsightEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInsight(insight: InsightEntity): Long
    
    @Update
    suspend fun updateInsight(insight: InsightEntity)
    
    @Delete
    suspend fun deleteInsight(insight: InsightEntity)
    
    @Query("DELETE FROM insights WHERE id = :insightId")
    suspend fun deleteInsightById(insightId: Long)
    
    @Query("SELECT COUNT(*) FROM insights WHERE book_id = :bookId")
    suspend fun getInsightCountForBook(bookId: Long): Int
    
    @Query("SELECT DISTINCT tags FROM insights WHERE tags IS NOT NULL AND tags != ''")
    suspend fun getAllTags(): List<String>
}