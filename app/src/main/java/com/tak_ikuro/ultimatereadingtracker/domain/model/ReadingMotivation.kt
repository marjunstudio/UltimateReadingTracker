package com.tak_ikuro.ultimatereadingtracker.domain.model

import com.tak_ikuro.ultimatereadingtracker.data.local.database.entity.MotivationType
import java.util.Date

data class ReadingMotivation(
    val id: Long = 0,
    val bookId: Long,
    val motivationType: MotivationType,
    val motivationDetail: String? = null,
    val createdAt: Date = Date()
) {
    fun validate(): Result<Unit> {
        return when {
            motivationDetail != null && motivationDetail.length > 500 -> Result.failure(IllegalArgumentException("Motivation detail must be less than 500 characters"))
            else -> Result.success(Unit)
        }
    }
}