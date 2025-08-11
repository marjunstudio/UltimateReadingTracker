package com.tak_ikuro.ultimatereadingtracker.data.repository

import com.tak_ikuro.ultimatereadingtracker.data.local.database.dao.BookDao
import com.tak_ikuro.ultimatereadingtracker.data.local.database.entity.BookEntity
import com.tak_ikuro.ultimatereadingtracker.data.local.database.entity.ReadingStatus
import com.tak_ikuro.ultimatereadingtracker.domain.model.Book
import com.tak_ikuro.ultimatereadingtracker.domain.repository.BookRepository
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.Date

class BookRepositoryTest {
    private lateinit var bookDao: BookDao
    private lateinit var bookRepository: BookRepository

    @Before
    fun setup() {
        bookDao = mockk()
        bookRepository = BookRepositoryImpl(bookDao)
    }

    @Test
    fun `saveBook should insert book and return success`() = runBlocking {
        val book = createTestBook()
        
        coEvery { bookDao.getBookByIsbn(book.isbn!!) } returns null
        coEvery { bookDao.insertBook(any()) } returns 1L

        val result = bookRepository.saveBook(book)

        assertTrue(result.isSuccess)
        coVerify { bookDao.insertBook(any()) }
    }

    @Test
    fun `saveBook should return failure when ISBN already exists`() = runBlocking {
        val book = createTestBook()
        val existingEntity = createTestBookEntity()
        
        coEvery { bookDao.getBookByIsbn(book.isbn!!) } returns existingEntity

        val result = bookRepository.saveBook(book)

        assertTrue(result.isFailure)
        result.onFailure { error ->
            assertTrue(error.message?.contains("ISBN") == true)
        }
        coVerify(exactly = 0) { bookDao.insertBook(any()) }
    }

    @Test
    fun `updateBook should update existing book`() = runBlocking {
        val book = createTestBook()
        
        coEvery { bookDao.updateBook(any()) } just Runs

        val result = bookRepository.updateBook(book)

        assertTrue(result.isSuccess)
        coVerify { bookDao.updateBook(any()) }
    }

    @Test
    fun `deleteBook should delete book`() = runBlocking {
        val book = createTestBook()
        
        coEvery { bookDao.deleteBook(any()) } just Runs

        val result = bookRepository.deleteBook(book)

        assertTrue(result.isSuccess)
        coVerify { bookDao.deleteBook(any()) }
    }

    @Test
    fun `getBookById should return book when exists`() = runBlocking {
        val bookEntity = createTestBookEntity()
        
        every { bookDao.getBookById(1L) } returns flowOf(bookEntity)

        val result = bookRepository.getBookById(1L).first()

        assertNotNull(result)
        assertEquals(bookEntity.title, result?.title)
        assertEquals(bookEntity.author, result?.author)
    }

    @Test
    fun `getBookById should return null when not exists`() = runBlocking {
        every { bookDao.getBookById(999L) } returns flowOf(null)

        val result = bookRepository.getBookById(999L).first()

        assertNull(result)
    }

    @Test
    fun `getAllBooks should return all books`() = runBlocking {
        val bookEntities = listOf(
            createTestBookEntity(1L, "Book 1"),
            createTestBookEntity(2L, "Book 2"),
            createTestBookEntity(3L, "Book 3")
        )
        
        every { bookDao.getAllBooks() } returns flowOf(bookEntities)

        val result = bookRepository.getAllBooks().first()

        assertEquals(3, result.size)
        assertEquals("Book 1", result[0].title)
        assertEquals("Book 2", result[1].title)
        assertEquals("Book 3", result[2].title)
    }

    @Test
    fun `getBooksByStatus should return filtered books`() = runBlocking {
        val readingBooks = listOf(
            createTestBookEntity(1L, "Reading 1", ReadingStatus.READING),
            createTestBookEntity(2L, "Reading 2", ReadingStatus.READING)
        )
        
        every { bookDao.getBooksByReadingStatus(ReadingStatus.READING) } returns flowOf(readingBooks)

        val result = bookRepository.getBooksByStatus(ReadingStatus.READING).first()

        assertEquals(2, result.size)
        assertTrue(result.all { it.readingStatus == ReadingStatus.READING })
    }

    @Test
    fun `searchBooks should return matching books`() = runBlocking {
        val query = "Kotlin"
        val matchingBooks = listOf(
            createTestBookEntity(1L, "Kotlin Programming"),
            createTestBookEntity(2L, "Advanced Kotlin")
        )
        
        every { bookDao.searchBooks(query) } returns flowOf(matchingBooks)

        val result = bookRepository.searchBooks(query).first()

        assertEquals(2, result.size)
        assertTrue(result.all { it.title.contains("Kotlin") })
    }

    @Test
    fun `checkIsbnExists should return true when book exists`() = runBlocking {
        val isbn = "9784123456789"
        val existingBook = createTestBookEntity()
        
        coEvery { bookDao.getBookByIsbn(isbn) } returns existingBook

        val result = bookRepository.checkIsbnExists(isbn)

        assertTrue(result)
    }

    @Test
    fun `checkIsbnExists should return false when book not exists`() = runBlocking {
        val isbn = "9784123456789"
        
        coEvery { bookDao.getBookByIsbn(isbn) } returns null

        val result = bookRepository.checkIsbnExists(isbn)

        assertFalse(result)
    }

    @Test
    fun `getBookStatistics should return correct counts`() = runBlocking {
        coEvery { bookDao.getBookCount() } returns 10
        coEvery { bookDao.getBookCountByStatus(ReadingStatus.UNREAD) } returns 3
        coEvery { bookDao.getBookCountByStatus(ReadingStatus.READING) } returns 2
        coEvery { bookDao.getBookCountByStatus(ReadingStatus.FINISHED) } returns 5

        val stats = bookRepository.getBookStatistics()

        assertEquals(10, stats.totalBooks)
        assertEquals(3, stats.unreadBooks)
        assertEquals(2, stats.readingBooks)
        assertEquals(5, stats.finishedBooks)
    }

    private fun createTestBook(): Book {
        return Book(
            id = 1L,
            title = "Test Book",
            author = "Test Author",
            isbn = "9784123456789",
            publisher = "Test Publisher",
            publishedDate = Date(),
            description = "Test Description",
            pageCount = 300,
            thumbnailUrl = null,
            readingStatus = ReadingStatus.READING,
            createdAt = Date(),
            updatedAt = Date(),
            startDate = Date(),
            finishDate = null,
            rating = 4.0f
        )
    }

    private fun createTestBookEntity(
        id: Long = 1L,
        title: String = "Test Book",
        status: ReadingStatus = ReadingStatus.READING
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

    private fun Book.toEntity(): BookEntity {
        return BookEntity(
            id = id,
            title = title,
            author = author,
            isbn = isbn,
            publisher = publisher,
            publishedDate = publishedDate,
            description = description,
            pageCount = pageCount,
            thumbnailUrl = thumbnailUrl,
            readingStatus = readingStatus,
            rating = rating,
            startDate = startDate,
            finishDate = finishDate,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}