package com.tak_ikuro.ultimatereadingtracker.data.local.database.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tak_ikuro.ultimatereadingtracker.data.local.database.AppDatabase
import com.tak_ikuro.ultimatereadingtracker.data.local.database.entity.BookEntity
import com.tak_ikuro.ultimatereadingtracker.data.local.database.entity.ReadingStatus
import com.tak_ikuro.ultimatereadingtracker.data.local.database.entity.ReviewEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@RunWith(AndroidJUnit4::class)
class ReviewDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var reviewDao: ReviewDao
    private lateinit var bookDao: BookDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        reviewDao = database.reviewDao()
        bookDao = database.bookDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndGetReviewByBookId() = runBlocking {
        val book = createTestBook()
        bookDao.insertBook(book)

        val review = ReviewEntity(
            id = 1,
            bookId = book.id,
            reviewText = "Great book!",
            rating = 5.0f,
            isDraft = false,
            createdAt = Date(),
            updatedAt = Date()
        )

        reviewDao.insertReview(review)
        val loaded = reviewDao.getReviewByBookId(book.id).first()

        assertNotNull(loaded)
        assertEquals(review.reviewText, loaded?.reviewText)
        assertEquals(review.rating, loaded?.rating)
        assertEquals(review.isDraft, loaded?.isDraft)
    }

    @Test
    fun getAllReviews() = runBlocking {
        val book1 = createTestBook(1)
        val book2 = createTestBook(2)
        bookDao.insertBook(book1)
        bookDao.insertBook(book2)

        val reviews = listOf(
            createTestReview(1, book1.id, "Review 1"),
            createTestReview(2, book2.id, "Review 2")
        )

        reviews.forEach { reviewDao.insertReview(it) }
        val loadedReviews = reviewDao.getAllReviews().first()

        assertEquals(2, loadedReviews.size)
        assertTrue(loadedReviews.any { it.reviewText == "Review 1" })
        assertTrue(loadedReviews.any { it.reviewText == "Review 2" })
    }

    @Test
    fun updateReview() = runBlocking {
        val book = createTestBook()
        bookDao.insertBook(book)

        val review = createTestReview(1, book.id, "Original")
        reviewDao.insertReview(review)

        val updated = review.copy(
            reviewText = "Updated",
            rating = 3.0f,
            isDraft = true
        )
        reviewDao.updateReview(updated)

        val loaded = reviewDao.getReviewByBookId(book.id).first()
        assertEquals("Updated", loaded?.reviewText)
        assertEquals(3.0f, loaded?.rating)
        assertTrue(loaded?.isDraft == true)
    }

    @Test
    fun deleteReview() = runBlocking {
        val book = createTestBook()
        bookDao.insertBook(book)

        val review = createTestReview(1, book.id, "To Delete")
        reviewDao.insertReview(review)

        val loaded = reviewDao.getReviewByBookId(book.id).first()
        assertNotNull(loaded)

        reviewDao.deleteReview(review)
        val deleted = reviewDao.getReviewByBookId(book.id).first()
        assertNull(deleted)
    }

    @Test
    fun getDraftReviews() = runBlocking {
        val book1 = createTestBook(1)
        val book2 = createTestBook(2)
        val book3 = createTestBook(3)
        bookDao.insertBook(book1)
        bookDao.insertBook(book2)
        bookDao.insertBook(book3)

        val reviews = listOf(
            createTestReview(1, book1.id, "Draft 1", isDraft = true),
            createTestReview(2, book2.id, "Published", isDraft = false),
            createTestReview(3, book3.id, "Draft 2", isDraft = true)
        )

        reviews.forEach { reviewDao.insertReview(it) }
        val draftReviews = reviewDao.getDraftReviews().first()

        assertEquals(2, draftReviews.size)
        assertTrue(draftReviews.all { it.isDraft })
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

    private fun createTestReview(
        id: Long,
        bookId: Long,
        content: String,
        isDraft: Boolean = false
    ): ReviewEntity {
        return ReviewEntity(
            id = id,
            bookId = bookId,
            reviewText = content,
            rating = 4.0f,
            isDraft = isDraft,
            createdAt = Date(),
            updatedAt = Date()
        )
    }
}