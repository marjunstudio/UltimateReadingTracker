package com.tak_ikuro.ultimatereadingtracker.data.local.database.entity

import org.junit.Test
import org.junit.Assert.*
import java.util.Date

class BookEntityTest {

    @Test
    fun `valid book entity should be created successfully`() {
        val book = BookEntity(
            id = 1,
            isbn = "978-4-7741-9763-2",
            title = "テスト駆動開発",
            author = "Kent Beck",
            publisher = "オライリージャパン",
            publishedDate = Date(),
            description = "テスト駆動開発の入門書",
            pageCount = 344,
            thumbnailUrl = "https://example.com/image.jpg",
            readingStatus = ReadingStatus.READING,
            rating = 4.5f,
            startDate = Date(),
            finishDate = null,
            createdAt = Date(),
            updatedAt = Date()
        )

        assertEquals("978-4-7741-9763-2", book.isbn)
        assertEquals("テスト駆動開発", book.title)
        assertEquals(ReadingStatus.READING, book.readingStatus)
    }

    @Test
    fun `book without ISBN should be valid`() {
        val book = BookEntity(
            id = 1,
            isbn = null,
            title = "自費出版の本",
            author = "著者名",
            publisher = null,
            publishedDate = null,
            description = null,
            pageCount = null,
            thumbnailUrl = null,
            readingStatus = ReadingStatus.UNREAD,
            rating = null,
            startDate = null,
            finishDate = null,
            createdAt = Date(),
            updatedAt = Date()
        )

        assertNull(book.isbn)
        assertEquals("自費出版の本", book.title)
    }

    @Test
    fun `validateISBN should return true for valid ISBN-13`() {
        val validISBN13List = listOf(
            "978-4-7741-9763-2",
            "9784774197632",
            "978-0-596-52068-7"
        )

        validISBN13List.forEach { isbn ->
            assertTrue("$isbn should be valid", BookEntity.validateISBN(isbn))
        }
    }

    @Test
    fun `validateISBN should return true for valid ISBN-10`() {
        val validISBN10List = listOf(
            "4-7741-9763-0",
            "4774197630",
            "0-596-52068-9"
        )

        validISBN10List.forEach { isbn ->
            assertTrue("$isbn should be valid", BookEntity.validateISBN(isbn))
        }
    }

    @Test
    fun `validateISBN should return false for invalid ISBN`() {
        val invalidISBNList = listOf(
            "123456789",  // 桁数不足
            "abcdefghijk",  // 数字以外
            "",  // 空文字
            "978--7741-9763-2"  // フォーマット不正
        )

        invalidISBNList.forEach { isbn ->
            assertFalse("$isbn should be invalid", BookEntity.validateISBN(isbn))
        }
    }

    @Test
    fun `validateISBN should return true for null ISBN`() {
        assertTrue("null ISBN should be valid", BookEntity.validateISBN(null))
    }

    @Test
    fun `title validation should fail for empty title`() {
        assertFalse("Empty title should be invalid", BookEntity.validateTitle(""))
        assertFalse("Blank title should be invalid", BookEntity.validateTitle("   "))
    }

    @Test
    fun `title validation should pass for non-empty title`() {
        assertTrue("Valid title should pass", BookEntity.validateTitle("本のタイトル"))
        assertTrue("Title with spaces should pass", BookEntity.validateTitle("The Great Book"))
    }

    @Test
    fun `rating should be between 0 and 5`() {
        assertTrue("Rating 0 should be valid", BookEntity.validateRating(0f))
        assertTrue("Rating 2.5 should be valid", BookEntity.validateRating(2.5f))
        assertTrue("Rating 5 should be valid", BookEntity.validateRating(5f))
        assertTrue("Null rating should be valid", BookEntity.validateRating(null))
        
        assertFalse("Negative rating should be invalid", BookEntity.validateRating(-1f))
        assertFalse("Rating over 5 should be invalid", BookEntity.validateRating(5.1f))
    }

    @Test
    fun `page count should be positive`() {
        assertTrue("Positive page count should be valid", BookEntity.validatePageCount(100))
        assertTrue("Null page count should be valid", BookEntity.validatePageCount(null))
        
        assertFalse("Zero page count should be invalid", BookEntity.validatePageCount(0))
        assertFalse("Negative page count should be invalid", BookEntity.validatePageCount(-1))
    }

    @Test
    fun `finish date should be after start date`() {
        val startDate = Date(1000000)
        val finishDateValid = Date(2000000)
        val finishDateInvalid = Date(500000)

        assertTrue("Finish date after start date should be valid", 
            BookEntity.validateDates(startDate, finishDateValid))
        assertTrue("Null dates should be valid", 
            BookEntity.validateDates(null, null))
        assertTrue("Only start date should be valid", 
            BookEntity.validateDates(startDate, null))
        
        assertFalse("Finish date before start date should be invalid", 
            BookEntity.validateDates(startDate, finishDateInvalid))
    }

    @Test
    fun `reading status transitions should be validated`() {
        assertTrue("UNREAD to READING should be valid", 
            BookEntity.validateStatusTransition(ReadingStatus.UNREAD, ReadingStatus.READING))
        assertTrue("READING to FINISHED should be valid", 
            BookEntity.validateStatusTransition(ReadingStatus.READING, ReadingStatus.FINISHED))
        assertTrue("UNREAD to FINISHED should be valid", 
            BookEntity.validateStatusTransition(ReadingStatus.UNREAD, ReadingStatus.FINISHED))
        
        assertFalse("FINISHED to UNREAD should be invalid", 
            BookEntity.validateStatusTransition(ReadingStatus.FINISHED, ReadingStatus.UNREAD))
        assertFalse("FINISHED to READING should be invalid", 
            BookEntity.validateStatusTransition(ReadingStatus.FINISHED, ReadingStatus.READING))
    }
}