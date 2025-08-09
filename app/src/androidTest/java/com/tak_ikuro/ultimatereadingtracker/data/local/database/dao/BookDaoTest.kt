package com.tak_ikuro.ultimatereadingtracker.data.local.database.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tak_ikuro.ultimatereadingtracker.data.local.database.AppDatabase
import com.tak_ikuro.ultimatereadingtracker.data.local.database.entity.BookEntity
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
class BookDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var bookDao: BookDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        bookDao = database.bookDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndGetBookById() = runBlocking {
        val book = BookEntity(
            id = 1,
            title = "Test Book",
            author = "Test Author",
            isbn = "9784123456789",
            publisher = "Test Publisher",
            publishedDate = Date(),
            description = "Test Description",
            pageCount = 300,
            thumbnailUrl = "https://example.com/cover.jpg",
            readingStatus = ReadingStatus.READING,
            rating = 4.0f,
            startDate = Date(),
            finishDate = null,
            createdAt = Date(),
            updatedAt = Date()
        )

        bookDao.insertBook(book)
        val loaded = bookDao.getBookById(1).first()

        assertNotNull(loaded)
        assertEquals(book.title, loaded?.title)
        assertEquals(book.author, loaded?.author)
        assertEquals(book.isbn, loaded?.isbn)
        assertEquals(book.readingStatus, loaded?.readingStatus)
    }

    @Test
    fun getAllBooksFlow() = runBlocking {
        val books = listOf(
            createTestBook(1, "Book 1", ReadingStatus.UNREAD),
            createTestBook(2, "Book 2", ReadingStatus.READING),
            createTestBook(3, "Book 3", ReadingStatus.FINISHED)
        )

        books.forEach { bookDao.insertBook(it) }
        val loadedBooks = bookDao.getAllBooks().first()

        assertEquals(3, loadedBooks.size)
        assertTrue(loadedBooks.any { it.title == "Book 1" })
        assertTrue(loadedBooks.any { it.title == "Book 2" })
        assertTrue(loadedBooks.any { it.title == "Book 3" })
    }

    @Test
    fun getBooksByReadingStatus() = runBlocking {
        val books = listOf(
            createTestBook(1, "Book 1", ReadingStatus.READING),
            createTestBook(2, "Book 2", ReadingStatus.READING),
            createTestBook(3, "Book 3", ReadingStatus.FINISHED),
            createTestBook(4, "Book 4", ReadingStatus.UNREAD)
        )

        books.forEach { bookDao.insertBook(it) }
        val readingBooks = bookDao.getBooksByReadingStatus(ReadingStatus.READING).first()

        assertEquals(2, readingBooks.size)
        assertTrue(readingBooks.all { it.readingStatus == ReadingStatus.READING })
    }

    @Test
    fun updateBook() = runBlocking {
        val book = createTestBook(1, "Original Title", ReadingStatus.UNREAD)
        bookDao.insertBook(book)

        val updatedBook = book.copy(
            title = "Updated Title",
            readingStatus = ReadingStatus.READING,
            rating = 5.0f
        )
        bookDao.updateBook(updatedBook)

        val loaded = bookDao.getBookById(1).first()
        assertEquals("Updated Title", loaded?.title)
        assertEquals(ReadingStatus.READING, loaded?.readingStatus)
        assertEquals(5.0f, loaded?.rating)
    }

    @Test
    fun deleteBook() = runBlocking {
        val book = createTestBook(1, "Book to Delete", ReadingStatus.UNREAD)
        bookDao.insertBook(book)

        val loaded = bookDao.getBookById(1).first()
        assertNotNull(loaded)

        bookDao.deleteBook(book)
        val deleted = bookDao.getBookById(1).first()
        assertNull(deleted)
    }

    @Test
    fun searchBooks() = runBlocking {
        val books = listOf(
            createTestBook(1, "Kotlin Programming", ReadingStatus.READING),
            createTestBook(2, "Android Development", ReadingStatus.FINISHED),
            createTestBook(3, "Java Basics", ReadingStatus.UNREAD)
        )

        books.forEach { bookDao.insertBook(it) }
        
        val kotlinBooks = bookDao.searchBooks("Kotlin").first()
        assertEquals(1, kotlinBooks.size)
        assertEquals("Kotlin Programming", kotlinBooks[0].title)

        val programmingBooks = bookDao.searchBooks("Programming").first()
        assertEquals(1, programmingBooks.size)
    }

    @Test
    fun getBookByIsbn() = runBlocking {
        val book = createTestBook(1, "ISBN Test Book", ReadingStatus.UNREAD)
        bookDao.insertBook(book)

        val loaded = bookDao.getBookByIsbn("9784123456789")
        assertNotNull(loaded)
        assertEquals(book.title, loaded?.title)
    }

    @Test
    fun preventDuplicateIsbn() = runBlocking {
        val book1 = createTestBook(1, "Book 1", ReadingStatus.UNREAD)
        val book2 = createTestBook(2, "Book 2", ReadingStatus.READING)
        
        bookDao.insertBook(book1)
        
        try {
            bookDao.insertBook(book2)
            fail("Should not allow duplicate ISBN")
        } catch (e: Exception) {
            // Expected behavior
        }
    }

    private fun createTestBook(
        id: Long,
        title: String,
        status: ReadingStatus
    ): BookEntity {
        return BookEntity(
            id = id,
            title = title,
            author = "Test Author",
            isbn = "9784123456789",
            publisher = "Test Publisher",
            publishedDate = Date(),
            description = "Test Description",
            pageCount = 300,
            thumbnailUrl = null,
            readingStatus = status,
            rating = null,
            startDate = if (status != ReadingStatus.UNREAD) Date() else null,
            finishDate = if (status == ReadingStatus.FINISHED) Date() else null,
            createdAt = Date(),
            updatedAt = Date()
        )
    }
}