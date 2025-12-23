package com.app.mobile.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.app.mobile.data.database.dao.HiveDao
import com.app.mobile.data.database.dao.HubDao
import com.app.mobile.data.database.dao.QueenDao
import com.app.mobile.data.database.dao.UserDao
import com.app.mobile.data.database.dao.WorkDao
import com.app.mobile.data.database.entity.HiveEntity
import com.app.mobile.data.database.entity.HubEntity
import com.app.mobile.data.database.entity.NotificationEntity
import com.app.mobile.data.database.entity.QueenEntity
import com.app.mobile.data.database.entity.UserEntity
import com.app.mobile.data.database.entity.WorkEntity

@Database(
    entities = [
        UserEntity::class,
        HubEntity::class,
        HiveEntity::class,
        QueenEntity::class,
        WorkEntity::class,
        NotificationEntity::class
    ],
    version = 5,
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