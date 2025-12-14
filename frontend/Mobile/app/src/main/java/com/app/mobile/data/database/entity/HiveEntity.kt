package com.app.mobile.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "hives")
data class HiveEntity(
    @PrimaryKey
    val id: String,
    val name: String
)
