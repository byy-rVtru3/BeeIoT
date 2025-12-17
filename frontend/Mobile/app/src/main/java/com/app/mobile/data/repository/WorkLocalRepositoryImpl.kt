package com.app.mobile.data.repository

import com.app.mobile.data.database.dao.WorkDao
import com.app.mobile.domain.mappers.toDomain
import com.app.mobile.domain.mappers.toEntity
import com.app.mobile.domain.models.hives.WorkDomain
import com.app.mobile.domain.repository.WorkLocalRepository

class WorkLocalRepositoryImpl(private val workDao: WorkDao) : WorkLocalRepository {
    override suspend fun getWork(workId: String) = workDao.getWork(workId)?.toDomain()

    override suspend fun getWorks(hiveId: String) = workDao.getWorks(hiveId).map { it.toDomain() }

    override suspend fun saveWork(work: WorkDomain) = workDao.saveWork(work.toEntity())
}