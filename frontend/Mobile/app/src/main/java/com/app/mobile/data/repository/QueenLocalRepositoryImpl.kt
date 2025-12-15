package com.app.mobile.data.repository

import com.app.mobile.data.database.dao.QueenDao
import com.app.mobile.data.database.mappers.toDomain
import com.app.mobile.data.database.mappers.toEntity
import com.app.mobile.domain.models.hives.queen.QueenDomain
import com.app.mobile.domain.repository.QueenLocalRepository

class QueenLocalRepositoryImpl(private val queenDao: QueenDao) : QueenLocalRepository {
    override suspend fun getQueenById(queenId: String) =
        queenDao.getQueenById(queenId)?.toDomain()

    override suspend fun saveQueen(queen: QueenDomain) {
        queenDao.saveQueen(queen.toEntity())
    }

    override suspend fun getQueens() =
        queenDao.getQueens().map { it.toDomain() }

    override suspend fun addHiveToQueen(queenId: String, hiveId: String) {
        queenDao.addHiveToQueen(queenId, hiveId)
    }
}