package com.tak_ikuro.ultimatereadingtracker.presentation.viewmodel

import app.cash.turbine.test
import com.tak_ikuro.ultimatereadingtracker.domain.model.Review
import com.tak_ikuro.ultimatereadingtracker.domain.repository.ReviewRepository
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
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class ReviewViewModelTest {

    private lateinit var reviewRepository: ReviewRepository
    private lateinit var viewModel: ReviewViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val testReview = Review(
        id = 1,
        bookId = 100,
        reviewText = "素晴らしい本でした",
        rating = 4.5f,
        isDraft = false,
        createdAt = Date(),
        updatedAt = Date()
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        reviewRepository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `初期化時に既存のレビューを読み込む`() = runTest {
        every { reviewRepository.getReviewByBookId(100) } returns flowOf(testReview)
        
        viewModel = ReviewViewModel(100, reviewRepository)
        advanceUntilIdle()

        viewModel.reviewState.test {
            val state = awaitItem()
            assertTrue(state is UiState.Success)
            val review = (state as UiState.Success).data
            assertEquals(testReview.reviewText, review?.reviewText)
            assertEquals(testReview.rating, review?.rating)
        }
    }

    @Test
    fun `新規レビューの場合は空の状態になる`() = runTest {
        every { reviewRepository.getReviewByBookId(100) } returns flowOf(null)
        
        viewModel = ReviewViewModel(100, reviewRepository)
        advanceUntilIdle()

        viewModel.reviewState.test {
            val state = awaitItem()
            assertTrue(state is UiState.Success)
            val review = (state as UiState.Success).data
            assertEquals(null, review)
        }
    }

    @Test
    fun `レビュー内容を更新すると自動保存タイマーが開始される`() = runTest {
        every { reviewRepository.getReviewByBookId(100) } returns flowOf(null)
        coEvery { reviewRepository.saveReview(any()) } returns Result.success(1L)
        
        viewModel = ReviewViewModel(100, reviewRepository)
        advanceUntilIdle()
        
        viewModel.updateContent("新しいレビュー内容")
        
        // 自動保存前（2秒以内）はまだ保存されない
        advanceTimeBy(1500)
        coVerify(exactly = 0) { reviewRepository.saveReview(any()) }
        
        // 2秒後に自動保存される
        advanceTimeBy(600)
        coVerify(exactly = 1) { 
            reviewRepository.saveReview(match { 
                it.reviewText == "新しいレビュー内容" && it.isDraft == true
            }) 
        }
    }

    @Test
    fun `連続して内容を更新すると最後の更新のみ保存される`() = runTest {
        every { reviewRepository.getReviewByBookId(100) } returns flowOf(null)
        coEvery { reviewRepository.saveReview(any()) } returns Result.success(1L)
        
        viewModel = ReviewViewModel(100, reviewRepository)
        advanceUntilIdle()
        
        viewModel.updateContent("最初の内容")
        advanceTimeBy(1000)
        viewModel.updateContent("二番目の内容")
        advanceTimeBy(1000)
        viewModel.updateContent("最終的な内容")
        advanceTimeBy(2100)
        
        // 最後の内容のみが保存される
        coVerify(exactly = 1) { 
            reviewRepository.saveReview(match { 
                it.reviewText == "最終的な内容"
            }) 
        }
    }

    @Test
    fun `評価を更新できる`() = runTest {
        every { reviewRepository.getReviewByBookId(100) } returns flowOf(testReview)
        coEvery { reviewRepository.saveReview(any()) } returns Result.success(1L)
        
        viewModel = ReviewViewModel(100, reviewRepository)
        advanceUntilIdle()
        
        viewModel.updateRating(5.0f)
        advanceTimeBy(2100)
        
        coVerify { 
            reviewRepository.saveReview(match { 
                it.rating == 5.0f
            }) 
        }
    }

    @Test
    fun `レビューを保存できる`() = runTest {
        every { reviewRepository.getReviewByBookId(100) } returns flowOf(null)
        coEvery { reviewRepository.saveReview(any()) } returns Result.success(1L)
        
        viewModel = ReviewViewModel(100, reviewRepository)
        advanceUntilIdle()
        
        viewModel.updateContent("完成したレビュー")
        viewModel.updateRating(4.0f)
        viewModel.saveReview()
        advanceUntilIdle()
        
        val capturedReview = slot<Review>()
        coVerify { reviewRepository.saveReview(capture(capturedReview)) }
        
        assertEquals("完成したレビュー", capturedReview.captured.reviewText)
        assertEquals(4.0f, capturedReview.captured.rating)
        assertEquals(false, capturedReview.captured.isDraft)
    }

    @Test
    fun `レビューを削除できる`() = runTest {
        every { reviewRepository.getReviewByBookId(100) } returns flowOf(testReview)
        coEvery { reviewRepository.deleteReview(any()) } returns Result.success(Unit)
        
        viewModel = ReviewViewModel(100, reviewRepository)
        advanceUntilIdle()
        
        viewModel.deleteReview()
        advanceUntilIdle()
        
        viewModel.deleteState.test {
            val state = awaitItem()
            assertTrue(state is UiState.Success)
        }
        
        coVerify { reviewRepository.deleteReview(testReview) }
    }

    @Test
    fun `保存エラーが発生した場合はErrorになる`() = runTest {
        every { reviewRepository.getReviewByBookId(100) } returns flowOf(null)
        val error = Exception("保存エラー")
        coEvery { reviewRepository.saveReview(any()) } returns Result.failure(error)
        
        viewModel = ReviewViewModel(100, reviewRepository)
        advanceUntilIdle()
        
        viewModel.updateContent("エラーテスト")
        viewModel.saveReview()
        advanceUntilIdle()
        
        viewModel.saveState.test {
            val state = awaitItem()
            assertTrue(state is UiState.Error)
            assertEquals(error, (state as UiState.Error).exception)
        }
    }

    @Test
    fun `文字数カウントが正しく更新される`() = runTest {
        every { reviewRepository.getReviewByBookId(100) } returns flowOf(null)
        coEvery { reviewRepository.saveReview(any()) } returns Result.success(1L)
        
        viewModel = ReviewViewModel(100, reviewRepository)
        advanceUntilIdle()
        
        viewModel.updateContent("これは10文字です。")
        
        viewModel.characterCount.test {
            assertEquals(10, awaitItem())
        }
    }

    @Test
    fun `最大文字数を超えると警告が表示される`() = runTest {
        every { reviewRepository.getReviewByBookId(100) } returns flowOf(null)
        coEvery { reviewRepository.saveReview(any()) } returns Result.success(1L)
        
        viewModel = ReviewViewModel(100, reviewRepository)
        advanceUntilIdle()
        
        val longText = "あ".repeat(2001) // 2001文字
        viewModel.updateContent(longText)
        
        viewModel.isOverMaxLength.test {
            assertTrue(awaitItem())
        }
    }
}