package com.app.mobile.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "hubs")
data class HubEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val hiveId: Int,
    val name: String,
    val ipAddress: String,
    val port: Int,
)
