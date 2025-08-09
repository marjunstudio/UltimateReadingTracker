package com.tak_ikuro.ultimatereadingtracker.domain.model

import com.tak_ikuro.ultimatereadingtracker.data.local.database.entity.Importance
import java.util.Date

data class Insight(
    val id: Long = 0,
    val bookId: Long,
    val content: String,
    val importance: Importance = Importance.MEDIUM,
    val tags: List<String> = emptyList(),
    val pageNumber: Int? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) {
    fun validate(): Result<Unit> {
        return when {
            content.isBlank() -> Result.failure(IllegalArgumentException("Insight content cannot be blank"))
            content.length > 1000 -> Result.failure(IllegalArgumentException("Insight content must be less than 1000 characters"))
            tags.size > 10 -> Result.failure(IllegalArgumentException("Cannot have more than 10 tags"))
            tags.any { it.length > 50 } -> Result.failure(IllegalArgumentException("Tag must be less than 50 characters"))
            pageNumber != null && pageNumber <= 0 -> Result.failure(IllegalArgumentException("Page number must be positive"))
            else -> Result.success(Unit)
        }
    }
}