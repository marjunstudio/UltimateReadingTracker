package com.tak_ikuro.ultimatereadingtracker.domain.repository

import com.tak_ikuro.ultimatereadingtracker.data.local.database.entity.ReadingStatus
import com.tak_ikuro.ultimatereadingtracker.domain.model.Book
import com.tak_ikuro.ultimatereadingtracker.domain.model.BookStatistics
import kotlinx.coroutines.flow.Flow

interface BookRepository {
    suspend fun saveBook(book: Book): Result<Long>
    suspend fun updateBook(book: Book): Result<Unit>
    suspend fun deleteBook(book: Book): Result<Unit>
    fun getBookById(bookId: Long): Flow<Book?>
    suspend fun getBookByIsbn(isbn: String): Book?
    fun getAllBooks(): Flow<List<Book>>
    fun getBooksByStatus(status: ReadingStatus): Flow<List<Book>>
    fun searchBooks(query: String): Flow<List<Book>>
    suspend fun checkIsbnExists(isbn: String): Boolean
    suspend fun getBookStatistics(): BookStatistics
    fun getRecentlyFinishedBooks(limit: Int = 10): Flow<List<Book>>
    fun getTopRatedBooks(limit: Int = 10): Flow<List<Book>>
}