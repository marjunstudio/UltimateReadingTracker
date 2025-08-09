package com.tak_ikuro.ultimatereadingtracker.data.repository

import com.tak_ikuro.ultimatereadingtracker.data.local.database.dao.ReviewDao
import com.tak_ikuro.ultimatereadingtracker.data.local.database.entity.ReviewEntity
import com.tak_ikuro.ultimatereadingtracker.domain.model.Review
import com.tak_ikuro.ultimatereadingtracker.domain.repository.ReviewRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewRepositoryImpl @Inject constructor(
    private val reviewDao: ReviewDao
) : ReviewRepository {

    override suspend fun saveReview(review: Review): Result<Long> {
        return try {
            review.validate().fold(
                onSuccess = {
                    val id = reviewDao.insertReview(review.toEntity())
                    Result.success(id)
                },
                onFailure = { Result.failure(it) }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateReview(review: Review): Result<Unit> {
        return try {
            review.validate().fold(
                onSuccess = {
                    reviewDao.updateReview(review.toEntity())
                    Result.success(Unit)
                },
                onFailure = { Result.failure(it) }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteReview(review: Review): Result<Unit> {
        return try {
            reviewDao.deleteReview(review.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getReviewByBookId(bookId: Long): Flow<Review?> {
        return reviewDao.getReviewByBookId(bookId).map { entity ->
            entity?.toDomainModel()
        }
    }

    override fun getAllReviews(): Flow<List<Review>> {
        return reviewDao.getAllReviews().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getDraftReviews(): Flow<List<Review>> {
        return reviewDao.getDraftReviews().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun saveDraft(review: Review): Result<Long> {
        return try {
            val draftReview = review.copy(isDraft = true)
            draftReview.validate().fold(
                onSuccess = {
                    val id = reviewDao.insertReview(draftReview.toEntity())
                    Result.success(id)
                },
                onFailure = { Result.failure(it) }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun publishDraft(reviewId: Long): Result<Unit> {
        return try {
            val review = reviewDao.getReviewById(reviewId)
            if (review != null) {
                val publishedReview = review.copy(isDraft = false)
                reviewDao.updateReview(publishedReview)
                Result.success(Unit)
            } else {
                Result.failure(IllegalArgumentException("Review not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteDraftsByBookId(bookId: Long): Result<Unit> {
        return try {
            reviewDao.deleteDraftReviewsForBook(bookId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun Review.toEntity(): ReviewEntity {
        return ReviewEntity(
            id = id,
            bookId = bookId,
            reviewText = reviewText,
            rating = rating,
            isDraft = isDraft,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    private fun ReviewEntity.toDomainModel(): Review {
        return Review(
            id = id,
            bookId = bookId,
            reviewText = reviewText,
            rating = rating,
            isDraft = isDraft,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}