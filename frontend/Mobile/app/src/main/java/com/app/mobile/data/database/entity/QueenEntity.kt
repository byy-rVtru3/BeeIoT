package com.app.mobile.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("queens")
data class QueenEntity(
    @PrimaryKey
    val id: String,
    val hiveId: String?,
    val name: String,
    val stages: QueenLifecycleDbModel
)
