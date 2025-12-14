package com.app.mobile.data.repository

import com.app.mobile.data.database.dao.HiveDao
import com.app.mobile.data.database.mappers.toDomain
import com.app.mobile.data.database.mappers.toEntity
import com.app.mobile.domain.models.hives.queen.QueenDomain
import com.app.mobile.domain.repository.QueenLocalRepository

class QueenLocalRepositoryImpl(private val hiveDao: HiveDao) : QueenLocalRepository {
    override suspend fun getQueenById(queenId: String) =
        hiveDao.getQueenById(queenId)?.toDomain()

    override suspend fun saveQueen(queen: QueenDomain) {
        hiveDao.saveQueen(queen.toEntity())
    }

    override suspend fun getQueens() =
        hiveDao.getQueens().map { it.toDomain() }
}