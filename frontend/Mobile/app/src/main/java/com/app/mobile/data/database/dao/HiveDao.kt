package com.app.mobile.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.app.mobile.data.database.entity.HiveEntity
import com.app.mobile.data.database.entity.HiveWithDetails

@Dao
interface HiveDao {

    @Query("SELECT * FROM hives")
    suspend fun getHives(): List<HiveEntity>

    @Transaction
    @Query("SELECT * FROM hives WHERE id = :hiveId")
    suspend fun getHive(hiveId: Int): HiveWithDetails?
}