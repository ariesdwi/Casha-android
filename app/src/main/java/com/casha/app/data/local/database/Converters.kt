package com.casha.app.data.local.database

import androidx.room.TypeConverter
import com.casha.app.domain.model.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
    fun fromIncomeType(value: String?): IncomeType? {
        return value?.let { enumValueOf<IncomeType>(it) }
    }

    @TypeConverter
    fun incomeTypeToString(enum: IncomeType?): String? {
        return enum?.name
    }

    @TypeConverter
    fun fromIncomeFrequency(value: String?): IncomeFrequency? {
        return value?.let { enumValueOf<IncomeFrequency>(it) }
    }

    @TypeConverter
    fun incomeFrequencyToString(enum: IncomeFrequency?): String? {
        return enum?.name
    }

    @TypeConverter
    fun fromNotificationType(value: String?): NotificationType? {
        return value?.let { enumValueOf<NotificationType>(it) }
    }

    @TypeConverter
    fun notificationTypeToString(enum: NotificationType?): String? {
        return enum?.name
    }

    @TypeConverter
    fun fromStringMap(value: String?): Map<String, String>? {
        if (value == null) return null
        return try {
            Json.decodeFromString<Map<String, String>>(value)
        } catch (e: Exception) {
            emptyMap()
        }
    }

    @TypeConverter
    fun toStringMap(value: Map<String, String>?): String? {
        if (value == null) return null
        return try {
            Json.encodeToString(value)
        } catch (e: Exception) {
            "{}"
        }
    }
}
