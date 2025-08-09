package com.tak_ikuro.ultimatereadingtracker.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "reading_motivations",
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
        Index(value = ["motivation_type"])
    ]
)
data class ReadingMotivationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "book_id")
    val bookId: Long,
    
    @ColumnInfo(name = "motivation_type")
    val motivationType: MotivationType,
    
    @ColumnInfo(name = "motivation_detail")
    val motivationDetail: String? = null,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date()
) {
    companion object {
        const val MAX_DETAIL_LENGTH = 500
        
        fun validateMotivationDetail(detail: String?): Boolean {
            return detail == null || detail.length <= MAX_DETAIL_LENGTH
        }
    }
    
    init {
        require(validateMotivationDetail(motivationDetail)) { 
            "Motivation detail must be under $MAX_DETAIL_LENGTH characters" 
        }
    }
}