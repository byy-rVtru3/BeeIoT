package com.app.mobile.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "works")
data class WorkEntity(
    @PrimaryKey
    val id: String,
    val hiveId: String,
    val title: String,
    val text: String,
    val dateTime: LocalDateTime
)
