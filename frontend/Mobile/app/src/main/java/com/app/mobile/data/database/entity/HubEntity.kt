package com.app.mobile.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "hubs")
data class HubEntity(
    @PrimaryKey
    val id: String,
    val hiveId: String?,
    val name: String,
    val ipAddress: String,
    val port: Int,
)
