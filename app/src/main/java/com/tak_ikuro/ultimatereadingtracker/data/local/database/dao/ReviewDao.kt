package com.tak_ikuro.ultimatereadingtracker.data.local.database.dao

import androidx.room.*
import com.tak_ikuro.ultimatereadingtracker.data.local.database.entity.ReviewEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewDao {
    
    @Query("SELECT * FROM reviews WHERE book_id = :bookId ORDER BY created_at DESC LIMIT 1")
    fun getReviewByBookId(bookId: Long): Flow<ReviewEntity?>
    
    @Query("SELECT * FROM reviews ORDER BY created_at DESC")
    fun getAllReviews(): Flow<List<ReviewEntity>>
    
    @Query("SELECT * FROM reviews WHERE is_draft = 1 ORDER BY updated_at DESC")
    fun getDraftReviews(): Flow<List<ReviewEntity>>
    
    @Query("SELECT * FROM reviews WHERE book_id = :bookId AND is_draft = 0 ORDER BY created_at DESC LIMIT 1")
    suspend fun getLatestReviewForBook(bookId: Long): ReviewEntity?
    
    @Query("SELECT * FROM reviews WHERE book_id = :bookId AND is_draft = 1 ORDER BY updated_at DESC LIMIT 1")
    suspend fun getDraftReviewForBook(bookId: Long): ReviewEntity?
    
    @Query("SELECT * FROM reviews WHERE id = :reviewId")
    suspend fun getReviewById(reviewId: Long): ReviewEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReviewEntity): Long
    
    @Update
    suspend fun updateReview(review: ReviewEntity)
    
    @Delete
    suspend fun deleteReview(review: ReviewEntity)
    
    @Query("DELETE FROM reviews WHERE id = :reviewId")
    suspend fun deleteReviewById(reviewId: Long)
    
    @Query("DELETE FROM reviews WHERE book_id = :bookId AND is_draft = 1")
    suspend fun deleteDraftReviewsForBook(bookId: Long)
    
    @Query("SELECT COUNT(*) FROM reviews WHERE book_id = :bookId")
    suspend fun getReviewCountForBook(bookId: Long): Int
}