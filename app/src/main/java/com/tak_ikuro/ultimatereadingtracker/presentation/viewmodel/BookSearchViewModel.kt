package com.tak_ikuro.ultimatereadingtracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tak_ikuro.ultimatereadingtracker.domain.model.Book
import com.tak_ikuro.ultimatereadingtracker.domain.usecase.SaveBookUseCase
import com.tak_ikuro.ultimatereadingtracker.domain.usecase.SearchBooksUseCase
import com.tak_ikuro.ultimatereadingtracker.presentation.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookSearchViewModel @Inject constructor(
    private val searchBooksUseCase: SearchBooksUseCase,
    private val saveBookUseCase: SaveBookUseCase
) : ViewModel() {

    private val _searchResultState = MutableStateFlow<UiState<List<Book>>>(UiState.Loading)
    val searchResultState: StateFlow<UiState<List<Book>>> = _searchResultState.asStateFlow()

    private val _saveBookState = MutableStateFlow<UiState<Long>>(UiState.Loading)
    val saveBookState: StateFlow<UiState<Long>> = _saveBookState.asStateFlow()

    private var searchJob: Job? = null

    fun searchBooks(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500) // デバウンス処理
            _searchResultState.value = UiState.Loading
            try {
                searchBooksUseCase(query)
                    .catch { e ->
                        _searchResultState.value = UiState.Error(e)
                    }
                    .collect { books ->
                        _searchResultState.value = UiState.Success(books)
                    }
            } catch (e: Exception) {
                _searchResultState.value = UiState.Error(e)
            }
        }
    }

    fun saveBook(book: Book) {
        viewModelScope.launch {
            _saveBookState.value = UiState.Loading
            saveBookUseCase(book).fold(
                onSuccess = { id ->
                    _saveBookState.value = UiState.Success(id)
                },
                onFailure = { error ->
                    _saveBookState.value = UiState.Error(error)
                }
            )
        }
    }

    fun clearSearchResults() {
        _searchResultState.value = UiState.Loading
        searchJob?.cancel()
    }

    fun clearSaveBookState() {
        _saveBookState.value = UiState.Loading
    }
}