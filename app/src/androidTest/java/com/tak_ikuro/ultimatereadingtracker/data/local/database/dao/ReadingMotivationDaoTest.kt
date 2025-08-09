package com.tak_ikuro.ultimatereadingtracker.data.local.database.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tak_ikuro.ultimatereadingtracker.data.local.database.AppDatabase
import com.tak_ikuro.ultimatereadingtracker.data.local.database.entity.BookEntity
import com.tak_ikuro.ultimatereadingtracker.data.local.database.entity.MotivationType
import com.tak_ikuro.ultimatereadingtracker.data.local.database.entity.ReadingMotivationEntity
import com.tak_ikuro.ultimatereadingtracker.data.local.database.entity.ReadingStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@RunWith(AndroidJUnit4::class)
class ReadingMotivationDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var motivationDao: ReadingMotivationDao
    private lateinit var bookDao: BookDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        motivationDao = database.readingMotivationDao()
        bookDao = database.bookDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndGetMotivationByBookId() = runBlocking {
        val book = createTestBook()
        bookDao.insertBook(book)

        val motivation = ReadingMotivationEntity(
            id = 1,
            bookId = book.id,
            motivationType = MotivationType.RECOMMENDATION,
            motivationDetail = "Highly recommended by colleague",
            createdAt = Date()
        )

        motivationDao.insertMotivation(motivation)
        val loaded = motivationDao.getMotivationByBookId(book.id).first()

        assertNotNull(loaded)
        assertEquals(motivation.motivationType, loaded?.motivationType)
        assertEquals(motivation.motivationDetail, loaded?.motivationDetail)
    }

    @Test
    fun getAllMotivations() = runBlocking {
        val book1 = createTestBook(1)
        val book2 = createTestBook(2)
        bookDao.insertBook(book1)
        bookDao.insertBook(book2)

        val motivations = listOf(
            createTestMotivation(1, book1.id, MotivationType.INTEREST),
            createTestMotivation(2, book2.id, MotivationType.RECOMMENDATION)
        )

        motivations.forEach { motivationDao.insertMotivation(it) }
        val allMotivations = motivationDao.getAllMotivations().first()

        assertEquals(2, allMotivations.size)
    }

    @Test
    fun getMotivationsByType() = runBlocking {
        val book1 = createTestBook(1)
        val book2 = createTestBook(2)
        val book3 = createTestBook(3)
        bookDao.insertBook(book1)
        bookDao.insertBook(book2)
        bookDao.insertBook(book3)

        val motivations = listOf(
            createTestMotivation(1, book1.id, MotivationType.RECOMMENDATION),
            createTestMotivation(2, book2.id, MotivationType.INTEREST),
            createTestMotivation(3, book3.id, MotivationType.RECOMMENDATION)
        )

        motivations.forEach { motivationDao.insertMotivation(it) }
        
        val recommendations = motivationDao.getMotivationsByType(MotivationType.RECOMMENDATION).first()
        assertEquals(2, recommendations.size)
        assertTrue(recommendations.all { it.motivationType == MotivationType.RECOMMENDATION })
    }

    @Test
    fun updateMotivation() = runBlocking {
        val book = createTestBook()
        bookDao.insertBook(book)

        val motivation = createTestMotivation(1, book.id, MotivationType.INTEREST)
        motivationDao.insertMotivation(motivation)

        val updated = motivation.copy(
            motivationType = MotivationType.REFERENCE,
            motivationDetail = "Updated Detail"
        )
        motivationDao.updateMotivation(updated)

        val loaded = motivationDao.getMotivationByBookId(book.id).first()
        assertEquals(MotivationType.REFERENCE, loaded?.motivationType)
        assertEquals("Updated Detail", loaded?.motivationDetail)
    }

    @Test
    fun deleteMotivation() = runBlocking {
        val book = createTestBook()
        bookDao.insertBook(book)

        val motivation = createTestMotivation(1, book.id, MotivationType.INTEREST)
        motivationDao.insertMotivation(motivation)

        val loaded = motivationDao.getMotivationByBookId(book.id).first()
        assertNotNull(loaded)

        motivationDao.deleteMotivation(motivation)
        val deleted = motivationDao.getMotivationByBookId(book.id).first()
        assertNull(deleted)
    }

    @Test
    fun getMotivationStatistics() = runBlocking {
        val books = (1..5).map { createTestBook(it.toLong()) }
        books.forEach { bookDao.insertBook(it) }

        val motivations = listOf(
            createTestMotivation(1, 1, MotivationType.RECOMMENDATION),
            createTestMotivation(2, 2, MotivationType.RECOMMENDATION),
            createTestMotivation(3, 3, MotivationType.INTEREST),
            createTestMotivation(4, 4, MotivationType.REFERENCE),
            createTestMotivation(5, 5, MotivationType.RECOMMENDATION)
        )

        motivations.forEach { motivationDao.insertMotivation(it) }

        val recommendationCount = motivationDao.getCountByType(MotivationType.RECOMMENDATION)
        val interestCount = motivationDao.getCountByType(MotivationType.INTEREST)
        val referenceCount = motivationDao.getCountByType(MotivationType.REFERENCE)

        assertEquals(3, recommendationCount)
        assertEquals(1, interestCount)
        assertEquals(1, referenceCount)
    }

    private fun createTestBook(id: Long = 1): BookEntity {
        return BookEntity(
            id = id,
            title = "Test Book $id",
            author = "Test Author",
            isbn = "978412345678$id",
            publisher = "Test Publisher",
            publishedDate = Date(),
            description = "Test Description",
            pageCount = 300,
            thumbnailUrl = null,
            readingStatus = ReadingStatus.FINISHED,
            rating = null,
            startDate = Date(),
            finishDate = Date(),
            createdAt = Date(),
            updatedAt = Date()
        )
    }

    private fun createTestMotivation(
        id: Long,
        bookId: Long,
        type: MotivationType
    ): ReadingMotivationEntity {
        return ReadingMotivationEntity(
            id = id,
            bookId = bookId,
            motivationType = type,
            motivationDetail = "Test Detail",
            createdAt = Date()
        )
    }
}