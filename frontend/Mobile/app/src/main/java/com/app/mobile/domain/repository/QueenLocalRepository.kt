package com.app.mobile.domain.repository

import com.app.mobile.domain.models.hives.queen.QueenDomain

interface QueenLocalRepository {

    suspend fun saveQueen(queen: QueenDomain)

    suspend fun getQueenById(queenId: String): QueenDomain?

    suspend fun getQueens(): List<QueenDomain>

    suspend fun addHiveToQueen(queenId: String, hiveId: String)
}