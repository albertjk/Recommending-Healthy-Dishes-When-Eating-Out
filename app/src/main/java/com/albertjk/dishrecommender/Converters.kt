package com.albertjk.dishrecommender

import androidx.room.TypeConverter
import java.util.*

/**
 * Converts a java.sql.Date type to the equivalent Unix
 * timestamp, which Room can persist, and vice versa.
 */
class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}