package com.app.mobile.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey
    val id: String,
    val hiveId: String,
    val notificationType: NotificationType,
    val message: String,
    val dateTime: LocalDateTime,
)