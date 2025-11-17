package com.app.mobile.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.app.mobile.data.database.dao.UserDao
import com.app.mobile.data.database.entity.UserEntity

@Database(entities = [UserEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}