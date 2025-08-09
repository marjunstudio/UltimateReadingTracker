package com.tak_ikuro.ultimatereadingtracker.domain.model

import java.util.Date

data class Review(
    val id: Long = 0,
    val bookId: Long,
    val reviewText: String,
    val rating: Float? = null,
    val isDraft: Boolean = false,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) {
    fun validate(): Result<Unit> {
        return when {
            !isDraft && reviewText.isBlank() -> Result.failure(IllegalArgumentException("Review text cannot be blank for non-draft reviews"))
            rating != null && (rating < 0f || rating > 5f) -> Result.failure(IllegalArgumentException("Rating must be between 0 and 5"))
            reviewText.length > 10000 -> Result.failure(IllegalArgumentException("Review text must be less than 10000 characters"))
            else -> Result.success(Unit)
        }
    }
}