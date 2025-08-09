package com.tak_ikuro.ultimatereadingtracker.data.local.database.entity

import org.junit.Test
import org.junit.Assert.*
import java.util.Date

class ReviewEntityTest {

    @Test
    fun `valid review entity should be created successfully`() {
        val review = ReviewEntity(
            id = 1,
            bookId = 1,
            reviewText = "素晴らしい本でした。多くの学びがありました。",
            rating = 4.5f,
            isDraft = false,
            createdAt = Date(),
            updatedAt = Date()
        )

        assertEquals(1L, review.bookId)
        assertEquals("素晴らしい本でした。多くの学びがありました。", review.reviewText)
        assertEquals(4.5f, review.rating)
        assertFalse(review.isDraft)
    }

    @Test
    fun `review can be created as draft`() {
        val review = ReviewEntity(
            id = 1,
            bookId = 1,
            reviewText = "作成中...",
            rating = null,
            isDraft = true,
            createdAt = Date(),
            updatedAt = Date()
        )

        assertTrue(review.isDraft)
        assertNull(review.rating)
    }

    @Test
    fun `review text can be empty for draft`() {
        val review = ReviewEntity(
            id = 1,
            bookId = 1,
            reviewText = "",
            rating = null,
            isDraft = true,
            createdAt = Date(),
            updatedAt = Date()
        )

        assertEquals("", review.reviewText)
        assertTrue(review.isDraft)
    }

    @Test
    fun `review text validation should work correctly`() {
        assertTrue("Normal text should be valid", 
            ReviewEntity.validateReviewText("これは感想です", false))
        assertTrue("Empty text for draft should be valid", 
            ReviewEntity.validateReviewText("", true))
        assertTrue("Long text should be valid", 
            ReviewEntity.validateReviewText("a".repeat(5000), false))
        
        assertFalse("Empty text for non-draft should be invalid", 
            ReviewEntity.validateReviewText("", false))
        assertFalse("Text over 10000 chars should be invalid", 
            ReviewEntity.validateReviewText("a".repeat(10001), false))
    }

    @Test
    fun `rating validation should work correctly`() {
        assertTrue("Rating 0 should be valid", ReviewEntity.validateRating(0f))
        assertTrue("Rating 2.5 should be valid", ReviewEntity.validateRating(2.5f))
        assertTrue("Rating 5 should be valid", ReviewEntity.validateRating(5f))
        assertTrue("Null rating should be valid", ReviewEntity.validateRating(null))
        
        assertFalse("Negative rating should be invalid", ReviewEntity.validateRating(-1f))
        assertFalse("Rating over 5 should be invalid", ReviewEntity.validateRating(5.1f))
    }

    @Test
    fun `character count should be calculated correctly`() {
        val review = ReviewEntity(
            id = 1,
            bookId = 1,
            reviewText = "これは10文字です。",
            rating = 4f,
            isDraft = false,
            createdAt = Date(),
            updatedAt = Date()
        )

        assertEquals(10, review.characterCount)
    }

    @Test
    fun `review should be convertible between draft and final`() {
        val draft = ReviewEntity(
            id = 1,
            bookId = 1,
            reviewText = "下書き",
            rating = null,
            isDraft = true,
            createdAt = Date(),
            updatedAt = Date()
        )

        val finalized = draft.copy(
            reviewText = "完成した感想",
            rating = 4f,
            isDraft = false,
            updatedAt = Date()
        )

        assertTrue(draft.isDraft)
        assertFalse(finalized.isDraft)
        assertEquals(4f, finalized.rating)
    }
}