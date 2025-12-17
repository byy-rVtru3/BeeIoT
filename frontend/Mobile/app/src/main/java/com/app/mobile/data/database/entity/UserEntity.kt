package com.app.mobile.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: Int = 0,
    val name: String,
    val email: String,
    val password: String,
    val jwtToken: String?
)