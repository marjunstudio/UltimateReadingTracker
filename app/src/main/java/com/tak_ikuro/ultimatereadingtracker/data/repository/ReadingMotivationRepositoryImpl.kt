package com.tak_ikuro.ultimatereadingtracker.data.repository

import com.tak_ikuro.ultimatereadingtracker.data.local.database.dao.MotivationTypeCount
import com.tak_ikuro.ultimatereadingtracker.data.local.database.dao.ReadingMotivationDao
import com.tak_ikuro.ultimatereadingtracker.data.local.database.entity.MotivationType
import com.tak_ikuro.ultimatereadingtracker.data.local.database.entity.ReadingMotivationEntity
import com.tak_ikuro.ultimatereadingtracker.domain.model.ReadingMotivation
import com.tak_ikuro.ultimatereadingtracker.domain.repository.ReadingMotivationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReadingMotivationRepositoryImpl @Inject constructor(
    private val motivationDao: ReadingMotivationDao
) : ReadingMotivationRepository {

    override suspend fun saveMotivation(motivation: ReadingMotivation): Result<Long> {
        return try {
            motivation.validate().fold(
                onSuccess = {
                    val id = motivationDao.insertMotivation(motivation.toEntity())
                    Result.success(id)
                },
                onFailure = { Result.failure(it) }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateMotivation(motivation: ReadingMotivation): Result<Unit> {
        return try {
            motivation.validate().fold(
                onSuccess = {
                    motivationDao.updateMotivation(motivation.toEntity())
                    Result.success(Unit)
                },
                onFailure = { Result.failure(it) }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteMotivation(motivation: ReadingMotivation): Result<Unit> {
        return try {
            motivationDao.deleteMotivation(motivation.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getMotivationByBookId(bookId: Long): Flow<ReadingMotivation?> {
        return motivationDao.getMotivationByBookId(bookId).map { entity ->
            entity?.toDomainModel()
        }
    }

    override fun getAllMotivations(): Flow<List<ReadingMotivation>> {
        return motivationDao.getAllMotivations().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getMotivationsByType(type: MotivationType): Flow<List<ReadingMotivation>> {
        return motivationDao.getMotivationsByType(type).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun getMotivationStatistics(): Result<List<MotivationTypeCount>> {
        return try {
            val stats = motivationDao.getMotivationTypeStatistics()
            Result.success(stats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun ReadingMotivation.toEntity(): ReadingMotivationEntity {
        return ReadingMotivationEntity(
            id = id,
            bookId = bookId,
            motivationType = motivationType,
            motivationDetail = motivationDetail,
            createdAt = createdAt
        )
    }

    private fun ReadingMotivationEntity.toDomainModel(): ReadingMotivation {
        return ReadingMotivation(
            id = id,
            bookId = bookId,
            motivationType = motivationType,
            motivationDetail = motivationDetail,
            createdAt = createdAt
        )
    }
}