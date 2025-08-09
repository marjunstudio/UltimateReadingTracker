package com.tak_ikuro.ultimatereadingtracker.domain.repository

import com.tak_ikuro.ultimatereadingtracker.data.local.database.dao.MotivationTypeCount
import com.tak_ikuro.ultimatereadingtracker.data.local.database.entity.MotivationType
import com.tak_ikuro.ultimatereadingtracker.domain.model.ReadingMotivation
import kotlinx.coroutines.flow.Flow

interface ReadingMotivationRepository {
    suspend fun saveMotivation(motivation: ReadingMotivation): Result<Long>
    suspend fun updateMotivation(motivation: ReadingMotivation): Result<Unit>
    suspend fun deleteMotivation(motivation: ReadingMotivation): Result<Unit>
    fun getMotivationByBookId(bookId: Long): Flow<ReadingMotivation?>
    fun getAllMotivations(): Flow<List<ReadingMotivation>>
    fun getMotivationsByType(type: MotivationType): Flow<List<ReadingMotivation>>
    suspend fun getMotivationStatistics(): Result<List<MotivationTypeCount>>
}