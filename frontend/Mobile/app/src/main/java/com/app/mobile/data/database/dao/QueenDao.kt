package com.app.mobile.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.app.mobile.data.database.entity.QueenEntity

@Dao
interface QueenDao {
    @Query("UPDATE queens SET hiveId = :hiveId WHERE id = :queenId")
    suspend fun addHiveToQueen(queenId: String, hiveId: String)

    @Upsert
    suspend fun saveQueen(queen: QueenEntity)

    @Query("SELECT * FROM queens")
    suspend fun getQueens(): List<QueenEntity>

    @Query("SELECT * FROM queens WHERE id = :queenId")
    suspend fun getQueenById(queenId: String): QueenEntity?
}