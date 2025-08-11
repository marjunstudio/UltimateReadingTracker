package com.tak_ikuro.ultimatereadingtracker.presentation.viewmodel

import app.cash.turbine.test
import com.tak_ikuro.ultimatereadingtracker.domain.model.Insight
import com.tak_ikuro.ultimatereadingtracker.data.local.database.entity.Importance
import com.tak_ikuro.ultimatereadingtracker.domain.repository.InsightRepository
import com.tak_ikuro.ultimatereadingtracker.presentation.state.UiState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class InsightViewModelTest {

    private lateinit var insightRepository: InsightRepository
    private lateinit var viewModel: InsightViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val testInsight = Insight(
        id = 1,
        bookId = 100,
        content = "重要な学び",
        importance = Importance.HIGH,
        tags = listOf("ビジネス", "リーダーシップ"),
        createdAt = Date(),
        updatedAt = Date()
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        insightRepository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `初期化時に既存の学びを読み込む`() = runTest {
        val insights = listOf(testInsight)
        every { insightRepository.getInsightsByBookId(100) } returns flowOf(insights)
        coEvery { insightRepository.getAllTags() } returns Result.success(listOf("ビジネス", "リーダーシップ", "自己啓発"))
        
        viewModel = InsightViewModel(100, insightRepository)
        advanceUntilIdle()

        viewModel.insightsState.test {
            val state = awaitItem()
            assertTrue(state is UiState.Success)
            val loadedInsights = (state as UiState.Success).data
            assertEquals(1, loadedInsights.size)
            assertEquals(testInsight.content, loadedInsights[0].content)
        }
    }

    @Test
    fun `新規学びを追加できる`() = runTest {
        every { insightRepository.getInsightsByBookId(100) } returns flowOf(emptyList())
        coEvery { insightRepository.getAllTags() } returns Result.success(emptyList())
        coEvery { insightRepository.saveInsight(any()) } returns Result.success(1L)
        
        viewModel = InsightViewModel(100, insightRepository)
        advanceUntilIdle()
        
        viewModel.addInsight("新しい学び", Importance.MEDIUM, listOf("新タグ"))
        advanceUntilIdle()
        
        val capturedInsight = slot<Insight>()
        coVerify { insightRepository.saveInsight(capture(capturedInsight)) }
        
        assertEquals("新しい学び", capturedInsight.captured.content)
        assertEquals(Importance.MEDIUM, capturedInsight.captured.importance)
        assertEquals(listOf("新タグ"), capturedInsight.captured.tags)
    }

    @Test
    fun `既存の学びを更新できる`() = runTest {
        every { insightRepository.getInsightsByBookId(100) } returns flowOf(listOf(testInsight))
        coEvery { insightRepository.getAllTags() } returns Result.success(listOf("ビジネス"))
        coEvery { insightRepository.updateInsight(any()) } returns Result.success(Unit)
        
        viewModel = InsightViewModel(100, insightRepository)
        advanceUntilIdle()
        
        viewModel.updateInsight(
            testInsight.copy(
                content = "更新された学び",
                importance = Importance.LOW
            )
        )
        advanceUntilIdle()
        
        coVerify { 
            insightRepository.updateInsight(match { 
                it.content == "更新された学び" && it.importance == Importance.LOW
            }) 
        }
    }

    @Test
    fun `学びを削除できる`() = runTest {
        every { insightRepository.getInsightsByBookId(100) } returns flowOf(listOf(testInsight))
        coEvery { insightRepository.getAllTags() } returns Result.success(listOf("ビジネス"))
        coEvery { insightRepository.deleteInsight(any()) } returns Result.success(Unit)
        
        viewModel = InsightViewModel(100, insightRepository)
        advanceUntilIdle()
        
        viewModel.deleteInsight(testInsight)
        advanceUntilIdle()
        
        coVerify { insightRepository.deleteInsight(testInsight) }
    }

    @Test
    fun `タグで学びをフィルタリングできる`() = runTest {
        val insights = listOf(
            testInsight,
            testInsight.copy(id = 2, tags = listOf("自己啓発")),
            testInsight.copy(id = 3, tags = listOf("ビジネス", "マーケティング"))
        )
        every { insightRepository.getInsightsByBookId(100) } returns flowOf(insights)
        coEvery { insightRepository.getAllTags() } returns Result.success(listOf("ビジネス", "自己啓発", "マーケティング"))
        
        viewModel = InsightViewModel(100, insightRepository)
        advanceUntilIdle()
        
        viewModel.filterByTag("ビジネス")
        advanceUntilIdle()
        
        viewModel.filteredInsights.test {
            val filtered = awaitItem()
            assertEquals(2, filtered.size)
            assertTrue(filtered.all { it.tags.contains("ビジネス") })
        }
    }

    @Test
    fun `重要度で学びをフィルタリングできる`() = runTest {
        val insights = listOf(
            testInsight,
            testInsight.copy(id = 2, importance = Importance.MEDIUM),
            testInsight.copy(id = 3, importance = Importance.LOW)
        )
        every { insightRepository.getInsightsByBookId(100) } returns flowOf(insights)
        coEvery { insightRepository.getAllTags() } returns Result.success(emptyList())
        
        viewModel = InsightViewModel(100, insightRepository)
        advanceUntilIdle()
        
        viewModel.filterByImportance(Importance.HIGH)
        advanceUntilIdle()
        
        viewModel.filteredInsights.test {
            val filtered = awaitItem()
            assertEquals(1, filtered.size)
            assertEquals(Importance.HIGH, filtered[0].importance)
        }
    }

    @Test
    fun `利用可能なタグを取得できる`() = runTest {
        val tags = listOf("ビジネス", "リーダーシップ", "自己啓発", "マーケティング")
        every { insightRepository.getInsightsByBookId(100) } returns flowOf(emptyList())
        coEvery { insightRepository.getAllTags() } returns Result.success(tags)
        
        viewModel = InsightViewModel(100, insightRepository)
        advanceUntilIdle()
        
        viewModel.availableTags.test {
            val availableTags = awaitItem()
            assertEquals(4, availableTags.size)
            assertTrue(availableTags.containsAll(tags))
        }
    }

    @Test
    fun `新しいタグを追加できる`() = runTest {
        every { insightRepository.getInsightsByBookId(100) } returns flowOf(emptyList())
        coEvery { insightRepository.getAllTags() } returns Result.success(listOf("既存タグ"))
        
        viewModel = InsightViewModel(100, insightRepository)
        advanceUntilIdle()
        
        viewModel.addNewTag("新規タグ")
        
        viewModel.availableTags.test {
            val tags = awaitItem()
            assertTrue(tags.contains("新規タグ"))
        }
    }

    @Test
    fun `保存エラーが発生した場合はErrorになる`() = runTest {
        every { insightRepository.getInsightsByBookId(100) } returns flowOf(emptyList())
        coEvery { insightRepository.getAllTags() } returns Result.success(emptyList())
        val error = Exception("保存エラー")
        coEvery { insightRepository.saveInsight(any()) } returns Result.failure(error)
        
        viewModel = InsightViewModel(100, insightRepository)
        advanceUntilIdle()
        
        viewModel.addInsight("エラーテスト", Importance.HIGH, emptyList())
        advanceUntilIdle()
        
        viewModel.saveState.test {
            val state = awaitItem()
            assertTrue(state is UiState.Error)
            assertEquals(error, (state as UiState.Error).exception)
        }
    }

    @Test
    fun `フィルターをクリアできる`() = runTest {
        val insights = listOf(
            testInsight,
            testInsight.copy(id = 2, importance = Importance.MEDIUM),
            testInsight.copy(id = 3, importance = Importance.LOW)
        )
        every { insightRepository.getInsightsByBookId(100) } returns flowOf(insights)
        coEvery { insightRepository.getAllTags() } returns Result.success(emptyList())
        
        viewModel = InsightViewModel(100, insightRepository)
        advanceUntilIdle()
        
        viewModel.filterByImportance(Importance.HIGH)
        advanceUntilIdle()
        
        viewModel.clearFilters()
        advanceUntilIdle()
        
        viewModel.filteredInsights.test {
            val filtered = awaitItem()
            assertEquals(3, filtered.size)
        }
    }
}