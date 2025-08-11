package com.tak_ikuro.ultimatereadingtracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tak_ikuro.ultimatereadingtracker.data.local.database.entity.ReadingStatus
import com.tak_ikuro.ultimatereadingtracker.domain.model.Book
import com.tak_ikuro.ultimatereadingtracker.domain.repository.BookRepository
import com.tak_ikuro.ultimatereadingtracker.presentation.state.UiState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

@HiltViewModel(assistedFactory = BookDetailViewModel.Factory::class)
class BookDetailViewModel @AssistedInject constructor(
    @Assisted private val bookId: Long,
    private val bookRepository: BookRepository
) : ViewModel() {

    private val _bookState = MutableStateFlow<UiState<Book>>(UiState.Loading)
    val bookState: StateFlow<UiState<Book>> = _bookState.asStateFlow()

    private val _updateState = MutableStateFlow<UiState<Unit>>(UiState.Loading)
    val updateState: StateFlow<UiState<Unit>> = _updateState.asStateFlow()

    private val _deleteState = MutableStateFlow<UiState<Unit>>(UiState.Loading)
    val deleteState: StateFlow<UiState<Unit>> = _deleteState.asStateFlow()

    private var currentBook: Book? = null

    init {
        loadBook()
    }

    private fun loadBook() {
        viewModelScope.launch {
            bookRepository.getBookById(bookId).collect { book ->
                if (book != null) {
                    currentBook = book
                    _bookState.value = UiState.Success(book)
                } else {
                    _bookState.value = UiState.Error(
                        IllegalArgumentException("書籍が見つかりません")
                    )
                }
            }
        }
    }

    fun updateReadingStatus(status: ReadingStatus) {
        currentBook?.let { book ->
            val updatedBook = when (status) {
                ReadingStatus.READING -> book.copy(
                    readingStatus = status,
                    startDate = book.startDate ?: Date()
                )
                ReadingStatus.FINISHED -> book.copy(
                    readingStatus = status,
                    finishDate = book.finishDate ?: Date()
                )
                else -> book.copy(readingStatus = status)
            }
            updateBook(updatedBook)
        }
    }

    fun updateRating(rating: Float) {
        currentBook?.let { book ->
            updateBook(book.copy(rating = rating))
        }
    }

    fun updateStartDate(date: Date) {
        currentBook?.let { book ->
            updateBook(
                book.copy(
                    startDate = date,
                    readingStatus = if (book.readingStatus == ReadingStatus.UNREAD) {
                        ReadingStatus.READING
                    } else {
                        book.readingStatus
                    }
                )
            )
        }
    }

    fun updateFinishDate(date: Date) {
        currentBook?.let { book ->
            updateBook(
                book.copy(
                    finishDate = date,
                    readingStatus = ReadingStatus.FINISHED
                )
            )
        }
    }

    private fun updateBook(book: Book) {
        viewModelScope.launch {
            bookRepository.updateBook(book).fold(
                onSuccess = {
                    currentBook = book
                    _bookState.value = UiState.Success(book)
                    _updateState.value = UiState.Success(Unit)
                },
                onFailure = { error ->
                    _updateState.value = UiState.Error(error)
                }
            )
        }
    }

    fun deleteBook() {
        currentBook?.let { book ->
            viewModelScope.launch {
                bookRepository.deleteBook(book).fold(
                    onSuccess = {
                        _deleteState.value = UiState.Success(Unit)
                    },
                    onFailure = { error ->
                        _deleteState.value = UiState.Error(error)
                    }
                )
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(bookId: Long): BookDetailViewModel
    }
}