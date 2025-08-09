package com.tak_ikuro.ultimatereadingtracker.data.local.database.entity

enum class Importance(val displayName: String, val priority: Int) {
    HIGH("高", 3),
    MEDIUM("中", 2),
    LOW("低", 1)
}