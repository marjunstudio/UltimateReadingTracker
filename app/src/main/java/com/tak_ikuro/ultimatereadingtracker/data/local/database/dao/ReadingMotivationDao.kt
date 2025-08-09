package com.tak_ikuro.ultimatereadingtracker.data.local.database.dao

import androidx.room.*
import com.tak_ikuro.ultimatereadingtracker.data.local.database.entity.MotivationType
import com.tak_ikuro.ultimatereadingtracker.data.local.database.entity.ReadingMotivationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingMotivationDao {
    
    @Query("SELECT * FROM reading_motivations WHERE book_id = :bookId ORDER BY created_at DESC LIMIT 1")
    fun getMotivationByBookId(bookId: Long): Flow<ReadingMotivationEntity?>
    
    @Query("SELECT * FROM reading_motivations ORDER BY created_at DESC")
    fun getAllMotivations(): Flow<List<ReadingMotivationEntity>>
    
    @Query("SELECT * FROM reading_motivations WHERE motivation_type = :type ORDER BY created_at DESC")
    fun getMotivationsByType(type: MotivationType): Flow<List<ReadingMotivationEntity>>
    
    @Query("SELECT * FROM reading_motivations WHERE id = :motivationId")
    suspend fun getMotivationById(motivationId: Long): ReadingMotivationEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMotivation(motivation: ReadingMotivationEntity): Long
    
    @Update
    suspend fun updateMotivation(motivation: ReadingMotivationEntity)
    
    @Delete
    suspend fun deleteMotivation(motivation: ReadingMotivationEntity)
    
    @Query("DELETE FROM reading_motivations WHERE id = :motivationId")
    suspend fun deleteMotivationById(motivationId: Long)
    
    @Query("DELETE FROM reading_motivations WHERE book_id = :bookId")
    suspend fun deleteMotivationsForBook(bookId: Long)
    
    @Query("SELECT COUNT(*) FROM reading_motivations WHERE book_id = :bookId")
    suspend fun getMotivationCountForBook(bookId: Long): Int
    
    @Query("SELECT motivation_type, COUNT(*) as count FROM reading_motivations GROUP BY motivation_type ORDER BY count DESC")
    suspend fun getMotivationTypeStatistics(): List<MotivationTypeCount>
    
    @Query("SELECT COUNT(*) FROM reading_motivations WHERE motivation_type = :type")
    suspend fun getCountByType(type: MotivationType): Int
}

data class MotivationTypeCount(
    @ColumnInfo(name = "motivation_type") val motivationType: MotivationType,
    @ColumnInfo(name = "count") val count: Int
)