package com.tak_ikuro.ultimatereadingtracker.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "isbn")
    val isbn: String? = null,
    
    @ColumnInfo(name = "title")
    val title: String,
    
    @ColumnInfo(name = "author")
    val author: String? = null,
    
    @ColumnInfo(name = "publisher")
    val publisher: String? = null,
    
    @ColumnInfo(name = "published_date")
    val publishedDate: Date? = null,
    
    @ColumnInfo(name = "description")
    val description: String? = null,
    
    @ColumnInfo(name = "page_count")
    val pageCount: Int? = null,
    
    @ColumnInfo(name = "thumbnail_url")
    val thumbnailUrl: String? = null,
    
    @ColumnInfo(name = "reading_status")
    val readingStatus: ReadingStatus = ReadingStatus.UNREAD,
    
    @ColumnInfo(name = "rating")
    val rating: Float? = null,
    
    @ColumnInfo(name = "start_date")
    val startDate: Date? = null,
    
    @ColumnInfo(name = "finish_date")
    val finishDate: Date? = null,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Date = Date()
) {
    companion object {
        fun validateISBN(isbn: String?): Boolean {
            if (isbn == null) return true
            
            val cleanIsbn = isbn.replace("-", "").replace(" ", "")
            
            return when (cleanIsbn.length) {
                10 -> validateISBN10(cleanIsbn)
                13 -> validateISBN13(cleanIsbn)
                else -> false
            }
        }
        
        private fun validateISBN10(isbn: String): Boolean {
            // ISBN-10の簡易バリデーション（厳密なチェックディジット検証はスキップ）
            return isbn.matches(Regex("^[0-9]{9}[0-9Xx]$"))
        }
        
        private fun validateISBN13(isbn: String): Boolean {
            // ISBN-13の簡易バリデーション（厳密なチェックディジット検証はスキップ）
            return isbn.matches(Regex("^[0-9]{13}$")) || isbn.matches(Regex("^97[89][0-9]{10}$"))
        }
        
        fun validateTitle(title: String?): Boolean {
            return !title.isNullOrBlank()
        }
        
        fun validateRating(rating: Float?): Boolean {
            return rating == null || (rating >= 0f && rating <= 5f)
        }
        
        fun validatePageCount(pageCount: Int?): Boolean {
            return pageCount == null || pageCount > 0
        }
        
        fun validateDates(startDate: Date?, finishDate: Date?): Boolean {
            if (startDate == null || finishDate == null) return true
            return finishDate.after(startDate)
        }
        
        fun validateStatusTransition(from: ReadingStatus, to: ReadingStatus): Boolean {
            return when (from) {
                ReadingStatus.UNREAD -> true
                ReadingStatus.READING -> to != ReadingStatus.UNREAD
                ReadingStatus.FINISHED -> false
            }
        }
    }
    
    init {
        require(validateTitle(title)) { "Title must not be empty" }
        require(validateISBN(isbn)) { "Invalid ISBN format" }
        require(validateRating(rating)) { "Rating must be between 0 and 5" }
        require(validatePageCount(pageCount)) { "Page count must be positive" }
        require(validateDates(startDate, finishDate)) { "Finish date must be after start date" }
    }
}