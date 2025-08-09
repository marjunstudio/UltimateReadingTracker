package com.tak_ikuro.ultimatereadingtracker.data.local.database.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tak_ikuro.ultimatereadingtracker.data.local.database.AppDatabase
import com.tak_ikuro.ultimatereadingtracker.data.local.database.entity.BookEntity
import com.tak_ikuro.ultimatereadingtracker.data.local.database.entity.Importance
import com.tak_ikuro.ultimatereadingtracker.data.local.database.entity.InsightEntity
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
class InsightDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var insightDao: InsightDao
    private lateinit var bookDao: BookDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        insightDao = database.insightDao()
        bookDao = database.bookDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndGetInsightsByBookId() = runBlocking {
        val book = createTestBook()
        bookDao.insertBook(book)

        val insights = listOf(
            createTestInsight(1, book.id, "Insight 1", listOf("tag1", "tag2")),
            createTestInsight(2, book.id, "Insight 2", listOf("tag3"))
        )

        insights.forEach { insightDao.insertInsight(it) }
        val loaded = insightDao.getInsightsByBookId(book.id).first()

        assertEquals(2, loaded.size)
        assertTrue(loaded.any { it.insightText == "Insight 1" })
        assertTrue(loaded.any { it.insightText == "Insight 2" })
    }

    @Test
    fun getAllInsights() = runBlocking {
        val book1 = createTestBook(1)
        val book2 = createTestBook(2)
        bookDao.insertBook(book1)
        bookDao.insertBook(book2)

        val insights = listOf(
            createTestInsight(1, book1.id, "Book1 Insight"),
            createTestInsight(2, book2.id, "Book2 Insight")
        )

        insights.forEach { insightDao.insertInsight(it) }
        val allInsights = insightDao.getAllInsights().first()

        assertEquals(2, allInsights.size)
    }

    @Test
    fun searchInsightsByTag() = runBlocking {
        val book1 = createTestBook(1)
        val book2 = createTestBook(2)
        bookDao.insertBook(book1)
        bookDao.insertBook(book2)

        val insights = listOf(
            createTestInsight(1, book1.id, "Insight 1", listOf("python", "coding")),
            createTestInsight(2, book1.id, "Insight 2", listOf("java", "coding")),
            createTestInsight(3, book2.id, "Insight 3", listOf("python", "data"))
        )

        insights.forEach { insightDao.insertInsight(it) }
        
        val pythonInsights = insightDao.searchInsightsByTag("python").first()
        assertEquals(2, pythonInsights.size)
        assertTrue(pythonInsights.all { it.tags?.contains("python") == true })

        val codingInsights = insightDao.searchInsightsByTag("coding").first()
        assertEquals(2, codingInsights.size)
        assertTrue(codingInsights.all { it.tags?.contains("coding") == true })
    }

    @Test
    fun getInsightsByImportance() = runBlocking {
        val book = createTestBook()
        bookDao.insertBook(book)

        val insights = listOf(
            createTestInsight(1, book.id, "High", importance = Importance.HIGH),
            createTestInsight(2, book.id, "Medium", importance = Importance.MEDIUM),
            createTestInsight(3, book.id, "Also High", importance = Importance.HIGH)
        )

        insights.forEach { insightDao.insertInsight(it) }
        
        val highImportance = insightDao.getInsightsByImportance(Importance.HIGH).first()
        assertEquals(2, highImportance.size)
        assertTrue(highImportance.all { it.importance == Importance.HIGH })
    }

    @Test
    fun updateInsight() = runBlocking {
        val book = createTestBook()
        bookDao.insertBook(book)

        val insight = createTestInsight(1, book.id, "Original")
        insightDao.insertInsight(insight)

        val updated = insight.copy(
            insightText = "Updated Content",
            importance = Importance.LOW,
            tags = "updated,tag"
        )
        insightDao.updateInsight(updated)

        val loaded = insightDao.getInsightsByBookId(book.id).first()
        assertEquals(1, loaded.size)
        assertEquals("Updated Content", loaded[0].insightText)
        assertEquals(Importance.LOW, loaded[0].importance)
        assertTrue(loaded[0].tags?.contains("updated") == true)
    }

    @Test
    fun deleteInsight() = runBlocking {
        val book = createTestBook()
        bookDao.insertBook(book)

        val insight = createTestInsight(1, book.id, "To Delete")
        insightDao.insertInsight(insight)

        val loaded = insightDao.getInsightsByBookId(book.id).first()
        assertEquals(1, loaded.size)

        insightDao.deleteInsight(insight)
        val deleted = insightDao.getInsightsByBookId(book.id).first()
        assertEquals(0, deleted.size)
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

    private fun createTestInsight(
        id: Long,
        bookId: Long,
        content: String,
        tags: List<String> = emptyList(),
        importance: Importance = Importance.MEDIUM
    ): InsightEntity {
        return InsightEntity(
            id = id,
            bookId = bookId,
            insightText = content,
            importance = importance,
            tags = if (tags.isNotEmpty()) tags.joinToString(",") else null,
            pageNumber = null,
            createdAt = Date(),
            updatedAt = Date()
        )
    }
}