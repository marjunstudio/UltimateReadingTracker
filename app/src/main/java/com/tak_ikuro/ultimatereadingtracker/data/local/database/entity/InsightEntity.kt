package com.tak_ikuro.ultimatereadingtracker.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "insights",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["id"],
            childColumns = ["book_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["book_id"]),
        Index(value = ["importance"])
    ]
)
data class InsightEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "book_id")
    val bookId: Long,
    
    @ColumnInfo(name = "insight_text")
    val insightText: String,
    
    @ColumnInfo(name = "importance")
    val importance: Importance = Importance.MEDIUM,
    
    @ColumnInfo(name = "tags")
    val tags: String? = null,
    
    @ColumnInfo(name = "page_number")
    val pageNumber: Int? = null,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Date = Date()
) {
    fun getTagList(): List<String> {
        return tags?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
    }
    
    companion object {
        const val MAX_INSIGHT_LENGTH = 5000
        const val MAX_TAGS = 20
        const val MAX_TAG_LENGTH = 50
        
        fun validateInsightText(text: String?): Boolean {
            if (text.isNullOrBlank()) return false
            return text.length <= MAX_INSIGHT_LENGTH
        }
        
        fun validatePageNumber(pageNumber: Int?): Boolean {
            return pageNumber == null || pageNumber > 0
        }
        
        fun validateTags(tags: String?): Boolean {
            if (tags == null || tags.isEmpty()) return true
            
            val tagList = tags.split(",").map { it.trim() }
            if (tagList.size > MAX_TAGS) return false
            
            return tagList.all { it.length <= MAX_TAG_LENGTH }
        }
    }
    
    init {
        require(validateInsightText(insightText)) { 
            "Insight text must not be empty and must be under $MAX_INSIGHT_LENGTH characters" 
        }
        require(validatePageNumber(pageNumber)) { "Page number must be positive" }
        require(validateTags(tags)) { 
            "Tags must not exceed $MAX_TAGS tags and each tag must be under $MAX_TAG_LENGTH characters" 
        }
    }
}