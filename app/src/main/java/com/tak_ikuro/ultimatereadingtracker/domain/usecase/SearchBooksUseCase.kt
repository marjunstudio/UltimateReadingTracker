package com.tak_ikuro.ultimatereadingtracker.domain.usecase

import com.tak_ikuro.ultimatereadingtracker.domain.model.Book
import com.tak_ikuro.ultimatereadingtracker.domain.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchBooksUseCase @Inject constructor(
    private val bookRepository: BookRepository
) {
    suspend operator fun invoke(query: String): Flow<List<Book>> {
        if (query.isBlank()) {
            return bookRepository.getAllBooks()
        }
        return bookRepository.searchBooks(query)
    }
}