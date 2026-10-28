package com.domeos.agent.data.local.converters

import androidx.room.TypeConverter
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.DateTimeFormatter

/**
 * Room 类型转换器
 */
class Converters {

    @TypeConverter
    fun fromInstant(value: Instant?): Long? {
        return value?.epochMilliseconds
    }

    @TypeConverter
    fun toInstant(value: Long?): Instant? {
        return value?.let { Instant.ofEpochMilliseconds(it) }
    }

    @TypeConverter
    fun fromTimeZone(value: TimeZone?): String? {
        return value?.id
    }

    @TypeConverter
    fun toTimeZone(value: String?): TimeZone? {
        return value?.let { TimeZone.of(it) }
    }
}