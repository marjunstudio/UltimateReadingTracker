package com.tak_ikuro.ultimatereadingtracker.data.local.database.converter

import androidx.room.TypeConverter
import com.tak_ikuro.ultimatereadingtracker.data.local.database.entity.Importance
import com.tak_ikuro.ultimatereadingtracker.data.local.database.entity.MotivationType
import com.tak_ikuro.ultimatereadingtracker.data.local.database.entity.ReadingStatus
import java.util.Date

class Converters {
    
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromReadingStatus(status: ReadingStatus): String {
        return status.name
    }

    @TypeConverter
    fun toReadingStatus(status: String): ReadingStatus {
        return ReadingStatus.valueOf(status)
    }

    @TypeConverter
    fun fromImportance(importance: Importance): String {
        return importance.name
    }

    @TypeConverter
    fun toImportance(importance: String): Importance {
        return Importance.valueOf(importance)
    }

    @TypeConverter
    fun fromMotivationType(type: MotivationType): String {
        return type.name
    }

    @TypeConverter
    fun toMotivationType(type: String): MotivationType {
        return MotivationType.valueOf(type)
    }
}