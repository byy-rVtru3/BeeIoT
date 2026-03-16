package com.app.mobile.data.datastore

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.app.mobile.data.datastore.dao.HiveDao
import com.app.mobile.data.datastore.dao.HubDao
import com.app.mobile.data.datastore.dao.QueenDao
import com.app.mobile.data.datastore.dao.UserDao
import com.app.mobile.data.datastore.dao.WorkDao
import com.app.mobile.data.datastore.entity.HiveEntity
import com.app.mobile.data.datastore.entity.HubEntity
import com.app.mobile.data.datastore.entity.NotificationEntity
import com.app.mobile.data.datastore.entity.QueenEntity
import com.app.mobile.data.datastore.entity.UserEntity
import com.app.mobile.data.datastore.entity.WorkEntity

@Database(
    entities = [
        UserEntity::class,
        HubEntity::class,
        HiveEntity::class,
        QueenEntity::class,
        WorkEntity::class,
        NotificationEntity::class
    ],
    version = 7,
    exportSchema = false
)
@TypeConverters(AppConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao

    abstract fun hiveDao(): HiveDao

    abstract fun queenDao(): QueenDao

    abstract fun hubDao(): HubDao

    abstract fun workDao(): WorkDao
}