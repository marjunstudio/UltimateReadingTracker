package com.tak_ikuro.ultimatereadingtracker.domain.repository

import com.tak_ikuro.ultimatereadingtracker.data.local.database.entity.Importance
import com.tak_ikuro.ultimatereadingtracker.domain.model.Insight
import kotlinx.coroutines.flow.Flow

interface InsightRepository {
    suspend fun saveInsight(insight: Insight): Result<Long>
    suspend fun updateInsight(insight: Insight): Result<Unit>
    suspend fun deleteInsight(insight: Insight): Result<Unit>
    fun getInsightsByBookId(bookId: Long): Flow<List<Insight>>
    fun getAllInsights(): Flow<List<Insight>>
    fun searchInsightsByTag(tag: String): Flow<List<Insight>>
    fun getInsightsByImportance(importance: Importance): Flow<List<Insight>>
    suspend fun getAllTags(): Result<List<String>>
}