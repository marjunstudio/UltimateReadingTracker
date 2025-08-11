package com.tak_ikuro.ultimatereadingtracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tak_ikuro.ultimatereadingtracker.data.local.database.entity.Importance
import com.tak_ikuro.ultimatereadingtracker.domain.model.Insight
import com.tak_ikuro.ultimatereadingtracker.domain.repository.InsightRepository
import com.tak_ikuro.ultimatereadingtracker.presentation.state.UiState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Date

@HiltViewModel(assistedFactory = InsightViewModel.Factory::class)
class InsightViewModel @AssistedInject constructor(
    @Assisted private val bookId: Long,
    private val insightRepository: InsightRepository
) : ViewModel() {

    private val _insightsState = MutableStateFlow<UiState<List<Insight>>>(UiState.Loading)
    val insightsState: StateFlow<UiState<List<Insight>>> = _insightsState.asStateFlow()

    private val _saveState = MutableStateFlow<UiState<Long>>(UiState.Loading)
    val saveState: StateFlow<UiState<Long>> = _saveState.asStateFlow()

    private val _availableTags = MutableStateFlow<List<String>>(emptyList())
    val availableTags: StateFlow<List<String>> = _availableTags.asStateFlow()

    private val _selectedTag = MutableStateFlow<String?>(null)
    private val _selectedImportance = MutableStateFlow<Importance?>(null)

    private val _filteredInsights = MutableStateFlow<List<Insight>>(emptyList())
    val filteredInsights: StateFlow<List<Insight>> = _filteredInsights.asStateFlow()

    private var allInsights: List<Insight> = emptyList()

    init {
        loadInsights()
        loadTags()
        setupFilters()
    }

    private fun loadInsights() {
        viewModelScope.launch {
            insightRepository.getInsightsByBookId(bookId).collect { insights ->
                allInsights = insights
                _insightsState.value = UiState.Success(insights)
                applyFilters()
            }
        }
    }

    private fun loadTags() {
        viewModelScope.launch {
            insightRepository.getAllTags().fold(
                onSuccess = { tags ->
                    _availableTags.value = tags
                },
                onFailure = {
                    // タグの読み込みエラーは無視
                }
            )
        }
    }

    private fun setupFilters() {
        viewModelScope.launch {
            combine(
                _selectedTag,
                _selectedImportance
            ) { tag, importance ->
                Pair(tag, importance)
            }.collect {
                applyFilters()
            }
        }
    }

    private fun applyFilters() {
        var filtered = allInsights

        _selectedTag.value?.let { tag ->
            filtered = filtered.filter { it.tags.contains(tag) }
        }

        _selectedImportance.value?.let { importance ->
            filtered = filtered.filter { it.importance == importance }
        }

        _filteredInsights.value = filtered
    }

    fun addInsight(content: String, importance: Importance, tags: List<String>) {
        viewModelScope.launch {
            val insight = Insight(
                bookId = bookId,
                content = content,
                importance = importance,
                tags = tags,
                createdAt = Date(),
                updatedAt = Date()
            )

            insightRepository.saveInsight(insight).fold(
                onSuccess = { id ->
                    _saveState.value = UiState.Success(id)
                    updateAvailableTagsWithNew(tags)
                },
                onFailure = { error ->
                    _saveState.value = UiState.Error(error)
                }
            )
        }
    }

    fun updateInsight(insight: Insight) {
        viewModelScope.launch {
            insightRepository.updateInsight(insight).fold(
                onSuccess = {
                    updateAvailableTagsWithNew(insight.tags)
                },
                onFailure = { error ->
                    _saveState.value = UiState.Error(error)
                }
            )
        }
    }

    fun deleteInsight(insight: Insight) {
        viewModelScope.launch {
            insightRepository.deleteInsight(insight).fold(
                onSuccess = {
                    // 削除成功
                },
                onFailure = { error ->
                    _saveState.value = UiState.Error(error)
                }
            )
        }
    }

    fun filterByTag(tag: String) {
        _selectedTag.value = tag
    }

    fun filterByImportance(importance: Importance) {
        _selectedImportance.value = importance
    }

    fun clearFilters() {
        _selectedTag.value = null
        _selectedImportance.value = null
    }

    fun addNewTag(tag: String) {
        val currentTags = _availableTags.value.toMutableList()
        if (!currentTags.contains(tag)) {
            currentTags.add(tag)
            _availableTags.value = currentTags
        }
    }

    private fun updateAvailableTagsWithNew(newTags: List<String>) {
        val currentTags = _availableTags.value.toMutableSet()
        currentTags.addAll(newTags)
        _availableTags.value = currentTags.toList()
    }

    @AssistedFactory
    interface Factory {
        fun create(bookId: Long): InsightViewModel
    }
}