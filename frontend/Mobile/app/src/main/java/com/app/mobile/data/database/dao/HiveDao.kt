package com.app.mobile.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.app.mobile.data.database.entity.HiveEntity

@Dao
fun interface HiveDao {

    @Transaction
    @Query("SELECT * FROM hives")
    suspend fun getHives(): List<HiveEntity>

}