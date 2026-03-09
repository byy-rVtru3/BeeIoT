package com.app.mobile.data.datastore

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.app.mobile.data.datastore.entity.NotificationType
import com.app.mobile.data.datastore.entity.QueenLifecycleDbModel
import kotlinx.serialization.json.Json
import java.time.LocalDateTime

@ProvidedTypeConverter
class AppConverters(private val json: Json) {

    @TypeConverter
    fun fromDate(date: LocalDateTime?) = date.toString()

    @TypeConverter
    fun toDate(dateString: String): LocalDateTime? = LocalDateTime.parse(dateString)

    @TypeConverter
    fun fromQueenLifecycle(queenLifecycle: QueenLifecycleDbModel?): String? {
        return queenLifecycle?.let { json.encodeToString(it) }
    }

    @TypeConverter
    fun toQueenLifecycle(jsonString: String?): QueenLifecycleDbModel? {
        if (jsonString.isNullOrBlank()) return null
        return json.decodeFromString(jsonString)
    }

    @TypeConverter
    fun toNotificationType(type: String) = NotificationType.valueOf(type)

    @TypeConverter
    fun fromNotificationType(type: NotificationType) = type.name
}