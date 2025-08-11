package com.tak_ikuro.ultimatereadingtracker.data.repository

import com.tak_ikuro.ultimatereadingtracker.data.local.database.dao.BookDao
import com.tak_ikuro.ultimatereadingtracker.data.local.database.entity.BookEntity
import com.tak_ikuro.ultimatereadingtracker.data.local.database.entity.ReadingStatus
import com.tak_ikuro.ultimatereadingtracker.domain.model.Book
import com.tak_ikuro.ultimatereadingtracker.domain.model.BookStatistics
import com.tak_ikuro.ultimatereadingtracker.domain.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookRepositoryImpl @Inject constructor(
    private val bookDao: BookDao
) : BookRepository {

    override suspend fun saveBook(book: Book): Result<Long> {
        return try {
            book.validate().fold(
                onSuccess = {
                    if (book.isbn != null && checkIsbnExists(book.isbn)) {
                        Result.failure(IllegalArgumentException("Book with ISBN ${book.isbn} already exists"))
                    } else {
                        val id = bookDao.insertBook(book.toEntity())
                        Result.success(id)
                    }
                },
                onFailure = { Result.failure(it) }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateBook(book: Book): Result<Unit> {
        return try {
            book.validate().fold(
                onSuccess = {
                    bookDao.updateBook(book.toEntity())
                    Result.success(Unit)
                },
                onFailure = { Result.failure(it) }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteBook(book: Book): Result<Unit> {
        return try {
            bookDao.deleteBook(book.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getBookById(bookId: Long): Flow<Book?> {
        return bookDao.getBookById(bookId).map { entity ->
            entity?.toDomainModel()
        }
    }

    override suspend fun getBookByIsbn(isbn: String): Book? {
        return bookDao.getBookByIsbn(isbn)?.toDomainModel()
    }

    override fun getAllBooks(): Flow<List<Book>> {
        return bookDao.getAllBooks().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getBooksByStatus(status: ReadingStatus): Flow<List<Book>> {
        return bookDao.getBooksByReadingStatus(status).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun searchBooks(query: String): Flow<List<Book>> {
        return bookDao.searchBooks(query).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun checkIsbnExists(isbn: String): Boolean {
        return bookDao.getBookByIsbn(isbn) != null
    }

    override suspend fun getBookStatistics(): BookStatistics {
        return BookStatistics(
            totalBooks = bookDao.getBookCount(),
            unreadBooks = bookDao.getBookCountByStatus(ReadingStatus.UNREAD),
            readingBooks = bookDao.getBookCountByStatus(ReadingStatus.READING),
            finishedBooks = bookDao.getBookCountByStatus(ReadingStatus.FINISHED)
        )
    }

    override fun getRecentlyFinishedBooks(limit: Int): Flow<List<Book>> {
        return bookDao.getRecentlyFinishedBooks(limit).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getTopRatedBooks(limit: Int): Flow<List<Book>> {
        return bookDao.getTopRatedBooks(limit).map { entities ->
            entities.map { it.toDomainModel() }
        }
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

    private fun BookEntity.toDomainModel(): Book {
        return Book(
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
            createdAt = createdAt,
            updatedAt = updatedAt,
            startDate = startDate,
            finishDate = finishDate,
            rating = rating
        )
    }
}