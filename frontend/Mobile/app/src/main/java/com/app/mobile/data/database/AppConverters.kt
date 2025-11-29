package com.app.mobile.data.database

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.TypeConverter
import com.app.mobile.data.database.entity.NotificationType
import com.app.mobile.data.database.entity.QueenStage
import java.time.LocalDateTime

class AppConverters {

    @TypeConverter
    fun fromDate(date: LocalDateTime?) = date.toString()

    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun toDate(dateString: String): LocalDateTime? = LocalDateTime.parse(dateString)

    @TypeConverter
    fun fromQueenStage(stage: QueenStage) = stage.name

    @TypeConverter
    fun toQueenStage(stageName: String) = QueenStage.valueOf(stageName)

    @TypeConverter
    fun toNotificationType(type: String) = NotificationType.valueOf(type)

    @TypeConverter
    fun fromNotificationType(type: NotificationType) = type.name
}