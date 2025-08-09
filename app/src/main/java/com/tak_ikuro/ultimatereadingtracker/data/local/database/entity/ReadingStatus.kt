package com.tak_ikuro.ultimatereadingtracker.data.local.database.entity

enum class ReadingStatus(val displayName: String) {
    UNREAD("未読"),
    READING("読書中"),
    FINISHED("読了")
}