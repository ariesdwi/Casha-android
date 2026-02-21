package com.casha.app.data.local.database

import androidx.room.TypeConverter
import com.casha.app.domain.model.*
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
}
