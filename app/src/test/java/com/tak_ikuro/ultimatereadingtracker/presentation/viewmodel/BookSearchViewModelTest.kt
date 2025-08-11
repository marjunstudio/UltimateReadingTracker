package com.tak_ikuro.ultimatereadingtracker.presentation.viewmodel

import app.cash.turbine.test
import com.tak_ikuro.ultimatereadingtracker.domain.model.Book
import com.tak_ikuro.ultimatereadingtracker.domain.usecase.SaveBookUseCase
import com.tak_ikuro.ultimatereadingtracker.domain.usecase.SearchBooksUseCase
import com.tak_ikuro.ultimatereadingtracker.presentation.state.UiState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Date
import com.tak_ikuro.ultimatereadingtracker.data.local.database.entity.ReadingStatus

@OptIn(ExperimentalCoroutinesApi::class)
class BookSearchViewModelTest {

    private lateinit var searchBooksUseCase: SearchBooksUseCase
    private lateinit var saveBookUseCase: SaveBookUseCase
    private lateinit var viewModel: BookSearchViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        searchBooksUseCase = mockk()
        saveBookUseCase = mockk()
        viewModel = BookSearchViewModel(searchBooksUseCase, saveBookUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `初期状態はLoading`() = runTest {
        viewModel.searchResultState.test {
            assertEquals(UiState.Loading, awaitItem())
        }
    }

    @Test
    fun `検索クエリを入力すると500ms後に検索が実行される`() = runTest {
        val books = listOf(
            Book(
                id = 0,
                isbn = "9784123456789",
                title = "テスト書籍",
                author = "テスト著者",
                publisher = "テスト出版社",
                publishedDate = Date(),
                thumbnailUrl = null,
                readingStatus = ReadingStatus.UNREAD,
                createdAt = Date()
            )
        )
        coEvery { searchBooksUseCase("テスト") } returns flowOf(books)

        viewModel.searchBooks("テスト")
        
        // デバウンス時間前はまだ検索されない
        advanceTimeBy(400)
        coVerify(exactly = 0) { searchBooksUseCase("テスト") }
        
        // デバウンス時間後に検索される
        advanceTimeBy(200)
        coVerify(exactly = 1) { searchBooksUseCase("テスト") }
    }

    @Test
    fun `連続して検索クエリを入力すると最後のクエリのみ実行される`() = runTest {
        val books = listOf(
            Book(
                id = 0,
                isbn = "9784123456789",
                title = "最終結果",
                author = "テスト著者",
                publisher = "テスト出版社",
                publishedDate = Date(),
                thumbnailUrl = null,
                readingStatus = ReadingStatus.UNREAD,
                createdAt = Date()
            )
        )
        coEvery { searchBooksUseCase(any()) } returns flowOf(books)

        viewModel.searchBooks("テスト1")
        advanceTimeBy(300)
        viewModel.searchBooks("テスト2")
        advanceTimeBy(300)
        viewModel.searchBooks("テスト3")
        advanceTimeBy(600)

        // 最後のクエリのみ実行される
        coVerify(exactly = 0) { searchBooksUseCase("テスト1") }
        coVerify(exactly = 0) { searchBooksUseCase("テスト2") }
        coVerify(exactly = 1) { searchBooksUseCase("テスト3") }
    }

    @Test
    fun `書籍保存が成功するとSuccessを返す`() = runTest {
        val book = Book(
            id = 0,
            isbn = "9784123456789",
            title = "テスト書籍",
            author = "テスト著者",
            publisher = "テスト出版社",
            publishedDate = Date(),
            thumbnailUrl = null,
            readingStatus = ReadingStatus.UNREAD,
            createdAt = Date()
        )
        coEvery { saveBookUseCase(book) } returns Result.success(1L)

        viewModel.saveBook(book)
        advanceTimeBy(100)

        viewModel.saveBookState.test {
            val state = awaitItem()
            assertTrue(state is UiState.Success)
            assertEquals(1L, (state as UiState.Success).data)
        }
    }

    @Test
    fun `書籍保存が失敗するとErrorを返す`() = runTest {
        val book = Book(
            id = 0,
            isbn = "9784123456789",
            title = "テスト書籍",
            author = "テスト著者",
            publisher = "テスト出版社",
            publishedDate = Date(),
            thumbnailUrl = null,
            readingStatus = ReadingStatus.UNREAD,
            createdAt = Date()
        )
        val error = IllegalStateException("この書籍は既に登録されています")
        coEvery { saveBookUseCase(book) } returns Result.failure(error)

        viewModel.saveBook(book)
        advanceTimeBy(100)

        viewModel.saveBookState.test {
            val state = awaitItem()
            assertTrue(state is UiState.Error)
            assertEquals(error, (state as UiState.Error).exception)
        }
    }

    @Test
    fun `検索エラーが発生するとErrorStateになる`() = runTest {
        val error = Exception("ネットワークエラー")
        coEvery { searchBooksUseCase("エラー") } throws error

        viewModel.searchBooks("エラー")
        advanceTimeBy(600)

        viewModel.searchResultState.test {
            val state = awaitItem()
            assertTrue(state is UiState.Error)
        }
    }
}