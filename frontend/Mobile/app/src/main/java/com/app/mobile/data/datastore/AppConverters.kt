package com.app.mobile.data.datastore

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.app.mobile.data.datastore.entity.NotificationType
import kotlinx.serialization.json.Json
import java.time.LocalDateTime

@ProvidedTypeConverter
class AppConverters(private val json: Json) {

    @TypeConverter
    fun fromDate(date: LocalDateTime?) = date.toString()

    @TypeConverter
    fun toDate(dateString: String): LocalDateTime? = LocalDateTime.parse(dateString)

    @TypeConverter
    fun toNotificationType(type: String) = NotificationType.valueOf(type)

    @TypeConverter
    fun fromNotificationType(type: NotificationType) = type.name
}
