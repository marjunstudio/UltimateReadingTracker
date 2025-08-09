package com.tak_ikuro.ultimatereadingtracker.data.repository

import com.tak_ikuro.ultimatereadingtracker.data.local.database.dao.InsightDao
import com.tak_ikuro.ultimatereadingtracker.data.local.database.entity.Importance
import com.tak_ikuro.ultimatereadingtracker.data.local.database.entity.InsightEntity
import com.tak_ikuro.ultimatereadingtracker.domain.model.Insight
import com.tak_ikuro.ultimatereadingtracker.domain.repository.InsightRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InsightRepositoryImpl @Inject constructor(
    private val insightDao: InsightDao
) : InsightRepository {

    override suspend fun saveInsight(insight: Insight): Result<Long> {
        return try {
            insight.validate().fold(
                onSuccess = {
                    val id = insightDao.insertInsight(insight.toEntity())
                    Result.success(id)
                },
                onFailure = { Result.failure(it) }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateInsight(insight: Insight): Result<Unit> {
        return try {
            insight.validate().fold(
                onSuccess = {
                    insightDao.updateInsight(insight.toEntity())
                    Result.success(Unit)
                },
                onFailure = { Result.failure(it) }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteInsight(insight: Insight): Result<Unit> {
        return try {
            insightDao.deleteInsight(insight.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getInsightsByBookId(bookId: Long): Flow<List<Insight>> {
        return insightDao.getInsightsByBookId(bookId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getAllInsights(): Flow<List<Insight>> {
        return insightDao.getAllInsights().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun searchInsightsByTag(tag: String): Flow<List<Insight>> {
        return insightDao.searchInsightsByTag(tag).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getInsightsByImportance(importance: Importance): Flow<List<Insight>> {
        return insightDao.getInsightsByImportance(importance).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun getAllTags(): Result<List<String>> {
        return try {
            val tagsString = insightDao.getAllTags()
            val allTags = tagsString.flatMap { tagString ->
                tagString.split(",").map { it.trim() }
            }.distinct().sorted()
            Result.success(allTags)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun Insight.toEntity(): InsightEntity {
        return InsightEntity(
            id = id,
            bookId = bookId,
            insightText = content,
            importance = importance,
            tags = tags.joinToString(","),
            pageNumber = pageNumber,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    private fun InsightEntity.toDomainModel(): Insight {
        return Insight(
            id = id,
            bookId = bookId,
            content = insightText,
            importance = importance,
            tags = getTagList(),
            pageNumber = pageNumber,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}