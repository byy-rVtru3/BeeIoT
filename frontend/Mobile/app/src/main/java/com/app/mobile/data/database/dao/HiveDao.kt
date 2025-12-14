package com.app.mobile.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.app.mobile.data.database.entity.HiveEntity
import com.app.mobile.data.database.entity.HiveWithDetails
import com.app.mobile.data.database.entity.QueenEntity

@Dao
interface HiveDao {

    @Query("SELECT * FROM hives")
    suspend fun getHives(): List<HiveEntity>

    @Transaction
    @Query("SELECT * FROM hives WHERE id = :hiveId")
    suspend fun getHive(hiveId: String): HiveWithDetails?

    @Query("SELECT * FROM queens WHERE id = :queenId")
    suspend fun getQueenById(queenId: String): QueenEntity?

    @Query("SELECT * FROM hives WHERE id = :hiveId")
    suspend fun getHivePreview(hiveId: String): HiveEntity?

    @Upsert
    suspend fun saveQueen(queen: QueenEntity)

    @Query("SELECT * FROM queens")
    suspend fun getQueens(): List<QueenEntity>
}