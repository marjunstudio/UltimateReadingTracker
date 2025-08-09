package com.tak_ikuro.ultimatereadingtracker.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "reviews",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["id"],
            childColumns = ["book_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["book_id"])]
)
data class ReviewEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "book_id")
    val bookId: Long,
    
    @ColumnInfo(name = "review_text")
    val reviewText: String,
    
    @ColumnInfo(name = "rating")
    val rating: Float? = null,
    
    @ColumnInfo(name = "is_draft")
    val isDraft: Boolean = false,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Date = Date()
) {
    val characterCount: Int
        get() = reviewText.length
    
    companion object {
        const val MAX_REVIEW_LENGTH = 10000
        
        fun validateReviewText(text: String?, isDraft: Boolean): Boolean {
            if (text == null) return false
            if (!isDraft && text.isEmpty()) return false
            return text.length <= MAX_REVIEW_LENGTH
        }
        
        fun validateRating(rating: Float?): Boolean {
            return rating == null || (rating >= 0f && rating <= 5f)
        }
    }
    
    init {
        require(validateReviewText(reviewText, isDraft)) { 
            "Review text must not be empty for non-draft reviews and must be under $MAX_REVIEW_LENGTH characters" 
        }
        require(validateRating(rating)) { "Rating must be between 0 and 5" }
    }
}