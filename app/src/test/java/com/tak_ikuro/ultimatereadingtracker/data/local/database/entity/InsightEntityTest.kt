package com.tak_ikuro.ultimatereadingtracker.data.local.database.entity

import org.junit.Test
import org.junit.Assert.*
import java.util.Date

class InsightEntityTest {

    @Test
    fun `valid insight entity should be created successfully`() {
        val insight = InsightEntity(
            id = 1,
            bookId = 1,
            insightText = "この本から学んだ重要なポイント",
            importance = Importance.HIGH,
            tags = "ビジネス,マーケティング,戦略",
            pageNumber = 42,
            createdAt = Date(),
            updatedAt = Date()
        )

        assertEquals(1L, insight.bookId)
        assertEquals("この本から学んだ重要なポイント", insight.insightText)
        assertEquals(Importance.HIGH, insight.importance)
        assertEquals("ビジネス,マーケティング,戦略", insight.tags)
        assertEquals(42, insight.pageNumber)
    }

    @Test
    fun `insight can be created without optional fields`() {
        val insight = InsightEntity(
            id = 1,
            bookId = 1,
            insightText = "学び",
            importance = Importance.MEDIUM,
            tags = null,
            pageNumber = null,
            createdAt = Date(),
            updatedAt = Date()
        )

        assertNull(insight.tags)
        assertNull(insight.pageNumber)
    }

    @Test
    fun `tag list should be parsed correctly`() {
        val insight = InsightEntity(
            id = 1,
            bookId = 1,
            insightText = "学び",
            importance = Importance.LOW,
            tags = "タグ1,タグ2,タグ3",
            pageNumber = null,
            createdAt = Date(),
            updatedAt = Date()
        )

        val tagList = insight.getTagList()
        assertEquals(3, tagList.size)
        assertEquals("タグ1", tagList[0])
        assertEquals("タグ2", tagList[1])
        assertEquals("タグ3", tagList[2])
    }

    @Test
    fun `empty tag string should return empty list`() {
        val insight = InsightEntity(
            id = 1,
            bookId = 1,
            insightText = "学び",
            importance = Importance.MEDIUM,
            tags = "",
            pageNumber = null,
            createdAt = Date(),
            updatedAt = Date()
        )

        val tagList = insight.getTagList()
        assertTrue(tagList.isEmpty())
    }

    @Test
    fun `null tags should return empty list`() {
        val insight = InsightEntity(
            id = 1,
            bookId = 1,
            insightText = "学び",
            importance = Importance.MEDIUM,
            tags = null,
            pageNumber = null,
            createdAt = Date(),
            updatedAt = Date()
        )

        val tagList = insight.getTagList()
        assertTrue(tagList.isEmpty())
    }

    @Test
    fun `insight text validation should work correctly`() {
        assertTrue("Normal text should be valid", 
            InsightEntity.validateInsightText("これは学びです"))
        assertTrue("Long text should be valid", 
            InsightEntity.validateInsightText("a".repeat(5000)))
        
        assertFalse("Empty text should be invalid", 
            InsightEntity.validateInsightText(""))
        assertFalse("Blank text should be invalid", 
            InsightEntity.validateInsightText("   "))
        assertFalse("Text over 5000 chars should be invalid", 
            InsightEntity.validateInsightText("a".repeat(5001)))
    }

    @Test
    fun `page number validation should work correctly`() {
        assertTrue("Positive page number should be valid", 
            InsightEntity.validatePageNumber(100))
        assertTrue("Page 1 should be valid", 
            InsightEntity.validatePageNumber(1))
        assertTrue("Null page number should be valid", 
            InsightEntity.validatePageNumber(null))
        
        assertFalse("Zero page number should be invalid", 
            InsightEntity.validatePageNumber(0))
        assertFalse("Negative page number should be invalid", 
            InsightEntity.validatePageNumber(-1))
    }

    @Test
    fun `tags validation should work correctly`() {
        assertTrue("Normal tags should be valid", 
            InsightEntity.validateTags("tag1,tag2,tag3"))
        assertTrue("Single tag should be valid", 
            InsightEntity.validateTags("tag"))
        assertTrue("Null tags should be valid", 
            InsightEntity.validateTags(null))
        assertTrue("Empty tags should be valid", 
            InsightEntity.validateTags(""))
        
        assertFalse("Too many tags should be invalid", 
            InsightEntity.validateTags((1..21).joinToString(",") { "tag$it" }))
        assertFalse("Tag too long should be invalid", 
            InsightEntity.validateTags("a".repeat(51)))
    }

    @Test
    fun `insights can be sorted by importance`() {
        val insights = listOf(
            InsightEntity(1, 1, "Low", Importance.LOW, null, null, Date(), Date()),
            InsightEntity(2, 1, "High", Importance.HIGH, null, null, Date(), Date()),
            InsightEntity(3, 1, "Medium", Importance.MEDIUM, null, null, Date(), Date())
        )

        val sorted = insights.sortedByDescending { it.importance.priority }
        
        assertEquals(Importance.HIGH, sorted[0].importance)
        assertEquals(Importance.MEDIUM, sorted[1].importance)
        assertEquals(Importance.LOW, sorted[2].importance)
    }
}