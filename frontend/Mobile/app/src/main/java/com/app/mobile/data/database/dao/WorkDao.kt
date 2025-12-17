package com.app.mobile.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.app.mobile.data.database.entity.WorkEntity

@Dao
interface WorkDao {

    @Query("SELECT * FROM works WHERE id = :workId")
    suspend fun getWork(workId: String): WorkEntity?

    @Query("SELECT * FROM works WHERE hiveId = :hiveId")
    suspend fun getWorks(hiveId: String): List<WorkEntity>

    @Upsert
    suspend fun saveWork(work: WorkEntity)

}