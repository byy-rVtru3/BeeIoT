package com.app.mobile.data.datastore.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.app.mobile.data.datastore.entity.HiveEntity
import com.app.mobile.data.datastore.entity.HiveWithDetails

@Dao
interface HiveDao {

    @Query("SELECT * FROM hives")
    suspend fun getHives(): List<HiveEntity>

    @Transaction
    @Query("SELECT * FROM hives WHERE id = :hiveId")
    suspend fun getHive(hiveId: String): HiveWithDetails?

    @Query("SELECT * FROM hives WHERE id = :hiveId")
    suspend fun getHivePreview(hiveId: String): HiveEntity?

    @Upsert
    suspend fun saveHive(hive: HiveEntity)

}