package com.tak_ikuro.ultimatereadingtracker.data.local.database.entity

import org.junit.Test
import org.junit.Assert.*
import java.util.Date

class ReadingMotivationEntityTest {

    @Test
    fun `valid reading motivation entity should be created successfully`() {
        val motivation = ReadingMotivationEntity(
            id = 1,
            bookId = 1,
            motivationType = MotivationType.RECOMMENDATION,
            motivationDetail = "友人から強く勧められた",
            createdAt = Date()
        )

        assertEquals(1L, motivation.bookId)
        assertEquals(MotivationType.RECOMMENDATION, motivation.motivationType)
        assertEquals("友人から強く勧められた", motivation.motivationDetail)
    }

    @Test
    fun `reading motivation can be created without detail`() {
        val motivation = ReadingMotivationEntity(
            id = 1,
            bookId = 1,
            motivationType = MotivationType.BESTSELLER,
            motivationDetail = null,
            createdAt = Date()
        )

        assertEquals(MotivationType.BESTSELLER, motivation.motivationType)
        assertNull(motivation.motivationDetail)
    }

    @Test
    fun `all motivation types should be valid`() {
        val motivationTypes = MotivationType.values()
        
        motivationTypes.forEach { type ->
            val motivation = ReadingMotivationEntity(
                id = 1,
                bookId = 1,
                motivationType = type,
                motivationDetail = "詳細: ${type.displayName}",
                createdAt = Date()
            )
            
            assertEquals(type, motivation.motivationType)
            assertTrue(motivation.motivationDetail?.contains(type.displayName) ?: false)
        }
    }

    @Test
    fun `motivation detail validation should work correctly`() {
        assertTrue("Normal text should be valid", 
            ReadingMotivationEntity.validateMotivationDetail("これはきっかけの詳細です"))
        assertTrue("Null detail should be valid", 
            ReadingMotivationEntity.validateMotivationDetail(null))
        assertTrue("Empty detail should be valid", 
            ReadingMotivationEntity.validateMotivationDetail(""))
        assertTrue("Long text should be valid", 
            ReadingMotivationEntity.validateMotivationDetail("a".repeat(500)))
        
        assertFalse("Text over 500 chars should be invalid", 
            ReadingMotivationEntity.validateMotivationDetail("a".repeat(501)))
    }

    @Test
    fun `motivation types should have correct display names`() {
        assertEquals("推薦された", MotivationType.RECOMMENDATION.displayName)
        assertEquals("ベストセラー", MotivationType.BESTSELLER.displayName)
        assertEquals("著者のファン", MotivationType.AUTHOR_FAN.displayName)
        assertEquals("トピックへの興味", MotivationType.TOPIC_INTEREST.displayName)
        assertEquals("表紙デザイン", MotivationType.COVER_DESIGN.displayName)
        assertEquals("レビューを見て", MotivationType.REVIEW.displayName)
        assertEquals("プレゼント", MotivationType.GIFT.displayName)
        assertEquals("学習・研究", MotivationType.STUDY.displayName)
        assertEquals("仕事関連", MotivationType.WORK.displayName)
        assertEquals("その他", MotivationType.OTHER.displayName)
    }

    @Test
    fun `multiple motivations can be created for same book`() {
        val motivations = listOf(
            ReadingMotivationEntity(1, 1, MotivationType.RECOMMENDATION, "友人A", Date()),
            ReadingMotivationEntity(2, 1, MotivationType.AUTHOR_FAN, "前作が良かった", Date()),
            ReadingMotivationEntity(3, 1, MotivationType.TOPIC_INTEREST, "AI技術に興味", Date())
        )

        assertEquals(3, motivations.size)
        assertTrue(motivations.all { it.bookId == 1L })
        assertEquals(3, motivations.map { it.motivationType }.distinct().size)
    }
}