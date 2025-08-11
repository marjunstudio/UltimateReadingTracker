package com.tak_ikuro.ultimatereadingtracker.presentation.viewmodel

import app.cash.turbine.test
import com.tak_ikuro.ultimatereadingtracker.data.local.database.entity.ReadingStatus
import com.tak_ikuro.ultimatereadingtracker.domain.model.Book
import com.tak_ikuro.ultimatereadingtracker.domain.repository.BookRepository
import com.tak_ikuro.ultimatereadingtracker.presentation.state.UiState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
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
class BookDetailViewModelTest {

    private lateinit var bookRepository: BookRepository
    private lateinit var viewModel: BookDetailViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val testBook = Book(
        id = 1,
        isbn = "9784123456789",
        title = "テストタイトル",
        author = "テスト著者",
        publisher = "テスト出版社",
        publishedDate = Date(),
        description = "テスト説明",
        pageCount = 300,
        thumbnailUrl = "https://example.com/image.jpg",
        readingStatus = ReadingStatus.READING,
        rating = 4.5f,
        startDate = Date(),
        finishDate = null,
        createdAt = Date(),
        updatedAt = Date()
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        bookRepository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `初期化時に書籍詳細を読み込む`() = runTest {
        every { bookRepository.getBookById(1) } returns flowOf(testBook)
        
        viewModel = BookDetailViewModel(1, bookRepository)
        advanceUntilIdle()

        viewModel.bookState.test {
            val state = awaitItem()
            assertTrue(state is UiState.Success)
            val book = (state as UiState.Success).data
            assertEquals(testBook.title, book.title)
            assertEquals(testBook.author, book.author)
        }
    }

    @Test
    fun `書籍が見つからない場合はErrorになる`() = runTest {
        every { bookRepository.getBookById(999) } returns flowOf(null)
        
        viewModel = BookDetailViewModel(999, bookRepository)
        advanceUntilIdle()

        viewModel.bookState.test {
            val state = awaitItem()
            assertTrue(state is UiState.Error)
        }
    }

    @Test
    fun `読書ステータスを更新できる`() = runTest {
        every { bookRepository.getBookById(1) } returns flowOf(testBook)
        val updatedBook = testBook.copy(readingStatus = ReadingStatus.FINISHED)
        coEvery { bookRepository.updateBook(any()) } returns Result.success(Unit)
        
        viewModel = BookDetailViewModel(1, bookRepository)
        advanceUntilIdle()
        
        viewModel.updateReadingStatus(ReadingStatus.FINISHED)
        advanceUntilIdle()

        coVerify { 
            bookRepository.updateBook(match { 
                it.readingStatus == ReadingStatus.FINISHED 
            }) 
        }
    }

    @Test
    fun `評価を更新できる`() = runTest {
        every { bookRepository.getBookById(1) } returns flowOf(testBook)
        coEvery { bookRepository.updateBook(any()) } returns Result.success(Unit)
        
        viewModel = BookDetailViewModel(1, bookRepository)
        advanceUntilIdle()
        
        viewModel.updateRating(5.0f)
        advanceUntilIdle()

        coVerify { 
            bookRepository.updateBook(match { 
                it.rating == 5.0f 
            }) 
        }
    }

    @Test
    fun `開始日を設定できる`() = runTest {
        every { bookRepository.getBookById(1) } returns flowOf(testBook)
        coEvery { bookRepository.updateBook(any()) } returns Result.success(Unit)
        
        viewModel = BookDetailViewModel(1, bookRepository)
        advanceUntilIdle()
        
        val startDate = Date()
        viewModel.updateStartDate(startDate)
        advanceUntilIdle()

        coVerify { 
            bookRepository.updateBook(match { 
                it.startDate == startDate && it.readingStatus == ReadingStatus.READING
            }) 
        }
    }

    @Test
    fun `完了日を設定できる`() = runTest {
        every { bookRepository.getBookById(1) } returns flowOf(testBook)
        coEvery { bookRepository.updateBook(any()) } returns Result.success(Unit)
        
        viewModel = BookDetailViewModel(1, bookRepository)
        advanceUntilIdle()
        
        val finishDate = Date()
        viewModel.updateFinishDate(finishDate)
        advanceUntilIdle()

        coVerify { 
            bookRepository.updateBook(match { 
                it.finishDate == finishDate && it.readingStatus == ReadingStatus.FINISHED
            }) 
        }
    }

    @Test
    fun `書籍を削除できる`() = runTest {
        every { bookRepository.getBookById(1) } returns flowOf(testBook)
        coEvery { bookRepository.deleteBook(any()) } returns Result.success(Unit)
        
        viewModel = BookDetailViewModel(1, bookRepository)
        advanceUntilIdle()
        
        viewModel.deleteBook()
        advanceUntilIdle()

        viewModel.deleteState.test {
            val state = awaitItem()
            assertTrue(state is UiState.Success)
        }
        
        coVerify { bookRepository.deleteBook(testBook) }
    }

    @Test
    fun `書籍削除が失敗した場合はErrorになる`() = runTest {
        every { bookRepository.getBookById(1) } returns flowOf(testBook)
        val error = Exception("削除エラー")
        coEvery { bookRepository.deleteBook(any()) } returns Result.failure(error)
        
        viewModel = BookDetailViewModel(1, bookRepository)
        advanceUntilIdle()
        
        viewModel.deleteBook()
        advanceUntilIdle()

        viewModel.deleteState.test {
            val state = awaitItem()
            assertTrue(state is UiState.Error)
            assertEquals(error, (state as UiState.Error).exception)
        }
    }

    @Test
    fun `更新エラーが発生した場合はErrorになる`() = runTest {
        every { bookRepository.getBookById(1) } returns flowOf(testBook)
        val error = Exception("更新エラー")
        coEvery { bookRepository.updateBook(any()) } returns Result.failure(error)
        
        viewModel = BookDetailViewModel(1, bookRepository)
        advanceUntilIdle()
        
        viewModel.updateRating(3.0f)
        advanceUntilIdle()

        viewModel.updateState.test {
            val state = awaitItem()
            assertTrue(state is UiState.Error)
            assertEquals(error, (state as UiState.Error).exception)
        }
    }
}