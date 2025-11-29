package com.app.mobile.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("queens")
data class QueenEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val hiveId: Int,
    val name: String,
    val stage: QueenStage,
)
