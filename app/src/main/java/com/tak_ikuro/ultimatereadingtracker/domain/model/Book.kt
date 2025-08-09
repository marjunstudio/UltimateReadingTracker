package com.tak_ikuro.ultimatereadingtracker.domain.model

import com.tak_ikuro.ultimatereadingtracker.data.local.database.entity.ReadingStatus
import java.util.Date

data class Book(
    val id: Long = 0,
    val title: String,
    val author: String? = null,
    val isbn: String? = null,
    val publisher: String? = null,
    val publishedDate: Date? = null,
    val description: String? = null,
    val pageCount: Int? = null,
    val thumbnailUrl: String? = null,
    val readingStatus: ReadingStatus = ReadingStatus.UNREAD,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val startDate: Date? = null,
    val finishDate: Date? = null,
    val rating: Float? = null
) {
    fun validate(): Result<Unit> {
        return when {
            title.isBlank() -> Result.failure(IllegalArgumentException("Title cannot be blank"))
            author != null && author.isBlank() -> Result.failure(IllegalArgumentException("Author cannot be blank"))
            isbn != null && !isValidIsbn(isbn) -> Result.failure(IllegalArgumentException("Invalid ISBN format"))
            rating != null && (rating < 0f || rating > 5f) -> Result.failure(IllegalArgumentException("Rating must be between 0 and 5"))
            pageCount != null && pageCount <= 0 -> Result.failure(IllegalArgumentException("Page count must be positive"))
            else -> Result.success(Unit)
        }
    }

    private fun isValidIsbn(isbn: String): Boolean {
        val cleanIsbn = isbn.replace("-", "").replace(" ", "")
        return when (cleanIsbn.length) {
            10 -> isValidIsbn10(cleanIsbn)
            13 -> isValidIsbn13(cleanIsbn)
            else -> false
        }
    }

    private fun isValidIsbn10(isbn: String): Boolean {
        if (!isbn.matches(Regex("^[0-9]{9}[0-9X]$"))) return false
        
        var sum = 0
        for (i in 0..8) {
            sum += (10 - i) * (isbn[i] - '0')
        }
        val checkDigit = if (isbn[9] == 'X') 10 else isbn[9] - '0'
        sum += checkDigit
        
        return sum % 11 == 0
    }

    private fun isValidIsbn13(isbn: String): Boolean {
        if (!isbn.matches(Regex("^[0-9]{13}$"))) return false
        
        var sum = 0
        for (i in 0..11) {
            val digit = isbn[i] - '0'
            sum += if (i % 2 == 0) digit else digit * 3
        }
        val checkDigit = isbn[12] - '0'
        
        return (10 - (sum % 10)) % 10 == checkDigit
    }
}

data class BookStatistics(
    val totalBooks: Int = 0,
    val unreadBooks: Int = 0,
    val readingBooks: Int = 0,
    val finishedBooks: Int = 0
)