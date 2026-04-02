package com.app.mobile.data.datastore

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.app.mobile.data.datastore.dao.WorkDao
import com.app.mobile.data.datastore.entity.NotificationEntity
import com.app.mobile.data.datastore.entity.WorkEntity

@Database(
    entities = [
        WorkEntity::class,
        NotificationEntity::class
    ],
    version = 9,
    exportSchema = false
)
@TypeConverters(AppConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun workDao(): WorkDao
}
