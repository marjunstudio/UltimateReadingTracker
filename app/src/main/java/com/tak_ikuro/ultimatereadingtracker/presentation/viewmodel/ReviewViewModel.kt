package com.tak_ikuro.ultimatereadingtracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tak_ikuro.ultimatereadingtracker.domain.model.Review
import com.tak_ikuro.ultimatereadingtracker.domain.repository.ReviewRepository
import com.tak_ikuro.ultimatereadingtracker.presentation.state.UiState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

@HiltViewModel(assistedFactory = ReviewViewModel.Factory::class)
class ReviewViewModel @AssistedInject constructor(
    @Assisted private val bookId: Long,
    private val reviewRepository: ReviewRepository
) : ViewModel() {

    companion object {
        private const val AUTO_SAVE_DELAY_MS = 2000L
        private const val MAX_REVIEW_LENGTH = 2000
    }

    private val _reviewState = MutableStateFlow<UiState<Review?>>(UiState.Loading)
    val reviewState: StateFlow<UiState<Review?>> = _reviewState.asStateFlow()

    private val _saveState = MutableStateFlow<UiState<Long>>(UiState.Loading)
    val saveState: StateFlow<UiState<Long>> = _saveState.asStateFlow()

    private val _deleteState = MutableStateFlow<UiState<Unit>>(UiState.Loading)
    val deleteState: StateFlow<UiState<Unit>> = _deleteState.asStateFlow()

    private val _characterCount = MutableStateFlow(0)
    val characterCount: StateFlow<Int> = _characterCount.asStateFlow()

    private val _isOverMaxLength = MutableStateFlow(false)
    val isOverMaxLength: StateFlow<Boolean> = _isOverMaxLength.asStateFlow()

    private var currentReview: Review? = null
    private var autoSaveJob: Job? = null
    private var pendingContent: String = ""
    private var pendingRating: Float? = null

    init {
        loadReview()
    }

    private fun loadReview() {
        viewModelScope.launch {
            reviewRepository.getReviewByBookId(bookId).collect { review ->
                currentReview = review
                _reviewState.value = UiState.Success(review)
                if (review != null) {
                    pendingContent = review.reviewText
                    pendingRating = review.rating
                    updateCharacterCount(review.reviewText)
                }
            }
        }
    }

    fun updateContent(content: String) {
        pendingContent = content
        updateCharacterCount(content)
        scheduleAutoSave()
    }

    fun updateRating(rating: Float) {
        pendingRating = rating
        scheduleAutoSave()
    }

    private fun updateCharacterCount(content: String) {
        _characterCount.value = content.length
        _isOverMaxLength.value = content.length > MAX_REVIEW_LENGTH
    }

    private fun scheduleAutoSave() {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(AUTO_SAVE_DELAY_MS)
            saveAsDraft()
        }
    }

    private suspend fun saveAsDraft() {
        val review = currentReview?.copy(
            reviewText = pendingContent,
            rating = pendingRating,
            isDraft = true,
            updatedAt = Date()
        ) ?: Review(
            bookId = bookId,
            reviewText = pendingContent,
            rating = pendingRating,
            isDraft = true
        )

        reviewRepository.saveReview(review).fold(
            onSuccess = { id ->
                if (currentReview == null) {
                    currentReview = review.copy(id = id)
                } else {
                    currentReview = review
                }
            },
            onFailure = {
                // 自動保存のエラーは表示しない
            }
        )
    }

    fun saveReview() {
        autoSaveJob?.cancel()
        viewModelScope.launch {
            val review = currentReview?.copy(
                reviewText = pendingContent,
                rating = pendingRating,
                isDraft = false,
                updatedAt = Date()
            ) ?: Review(
                bookId = bookId,
                reviewText = pendingContent,
                rating = pendingRating,
                isDraft = false
            )

            reviewRepository.saveReview(review).fold(
                onSuccess = { id ->
                    _saveState.value = UiState.Success(id)
                    currentReview = if (currentReview == null) {
                        review.copy(id = id)
                    } else {
                        review
                    }
                },
                onFailure = { error ->
                    _saveState.value = UiState.Error(error)
                }
            )
        }
    }

    fun deleteReview() {
        currentReview?.let { review ->
            viewModelScope.launch {
                reviewRepository.deleteReview(review).fold(
                    onSuccess = {
                        _deleteState.value = UiState.Success(Unit)
                        currentReview = null
                        pendingContent = ""
                        pendingRating = null
                        _characterCount.value = 0
                    },
                    onFailure = { error ->
                        _deleteState.value = UiState.Error(error)
                    }
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        autoSaveJob?.cancel()
    }

    @AssistedFactory
    interface Factory {
        fun create(bookId: Long): ReviewViewModel
    }
}