package com.app.mobile.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "hives")
data class HiveEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String
)
