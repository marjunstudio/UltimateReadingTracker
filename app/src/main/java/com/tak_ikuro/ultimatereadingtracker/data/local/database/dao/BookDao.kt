package com.tak_ikuro.ultimatereadingtracker.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tak_ikuro.ultimatereadingtracker.data.local.database.entity.BookEntity
import com.tak_ikuro.ultimatereadingtracker.data.local.database.entity.ReadingStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    
    @Query("SELECT * FROM books ORDER BY created_at DESC")
    fun getAllBooks(): Flow<List<BookEntity>>
    
    @Query("SELECT * FROM books WHERE reading_status = :status ORDER BY created_at DESC")
    fun getBooksByReadingStatus(status: ReadingStatus): Flow<List<BookEntity>>
    
    @Query("SELECT * FROM books WHERE id = :bookId")
    fun getBookById(bookId: Long): Flow<BookEntity?>
    
    @Query("SELECT * FROM books WHERE isbn = :isbn LIMIT 1")
    suspend fun getBookByIsbn(isbn: String): BookEntity?
    
    @Query("SELECT * FROM books WHERE title LIKE '%' || :query || '%' OR author LIKE '%' || :query || '%' ORDER BY created_at DESC")
    fun searchBooks(query: String): Flow<List<BookEntity>>
    
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertBook(book: BookEntity): Long
    
    @Update
    suspend fun updateBook(book: BookEntity)
    
    @Delete
    suspend fun deleteBook(book: BookEntity)
    
    @Query("DELETE FROM books WHERE id = :bookId")
    suspend fun deleteBookById(bookId: Long)
    
    @Query("SELECT COUNT(*) FROM books")
    suspend fun getBookCount(): Int
    
    @Query("SELECT COUNT(*) FROM books WHERE reading_status = :status")
    suspend fun getBookCountByStatus(status: ReadingStatus): Int
    
    @Query("SELECT * FROM books WHERE finish_date IS NOT NULL ORDER BY finish_date DESC LIMIT :limit")
    fun getRecentlyFinishedBooks(limit: Int = 10): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE rating IS NOT NULL ORDER BY rating DESC LIMIT :limit")
    fun getTopRatedBooks(limit: Int = 10): Flow<List<BookEntity>>
}