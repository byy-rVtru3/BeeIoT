package com.app.mobile.data.datastore

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.app.mobile.data.datastore.dao.UserDao
import com.app.mobile.data.datastore.dao.WorkDao
import com.app.mobile.data.datastore.entity.NotificationEntity
import com.app.mobile.data.datastore.entity.UserEntity
import com.app.mobile.data.datastore.entity.WorkEntity

@Database(
    entities = [
        UserEntity::class,
        WorkEntity::class,
        NotificationEntity::class
    ],
    version = 8,
    exportSchema = false
)
@TypeConverters(AppConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao

    abstract fun workDao(): WorkDao
}
