package com.tak_ikuro.ultimatereadingtracker.domain.repository

import com.tak_ikuro.ultimatereadingtracker.domain.model.Review
import kotlinx.coroutines.flow.Flow

interface ReviewRepository {
    suspend fun saveReview(review: Review): Result<Long>
    suspend fun updateReview(review: Review): Result<Unit>
    suspend fun deleteReview(review: Review): Result<Unit>
    fun getReviewByBookId(bookId: Long): Flow<Review?>
    fun getAllReviews(): Flow<List<Review>>
    fun getDraftReviews(): Flow<List<Review>>
    suspend fun saveDraft(review: Review): Result<Long>
    suspend fun publishDraft(reviewId: Long): Result<Unit>
    suspend fun deleteDraftsByBookId(bookId: Long): Result<Unit>
}