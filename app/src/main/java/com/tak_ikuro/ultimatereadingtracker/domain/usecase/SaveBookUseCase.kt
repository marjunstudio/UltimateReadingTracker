package com.tak_ikuro.ultimatereadingtracker.domain.usecase

import com.tak_ikuro.ultimatereadingtracker.domain.model.Book
import com.tak_ikuro.ultimatereadingtracker.domain.repository.BookRepository
import javax.inject.Inject

class SaveBookUseCase @Inject constructor(
    private val bookRepository: BookRepository
) {
    suspend operator fun invoke(book: Book): Result<Long> {
        return try {
            if (book.isbn != null) {
                val existingBook = bookRepository.getBookByIsbn(book.isbn)
                if (existingBook != null) {
                    return Result.failure(IllegalStateException("この書籍は既に登録されています"))
                }
            }
            bookRepository.saveBook(book)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}