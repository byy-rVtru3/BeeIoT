package com.app.mobile.data.repository

import com.app.mobile.data.database.dao.HiveDao
import com.app.mobile.domain.mappers.toDomain
import com.app.mobile.domain.models.hives.HiveDomainPreview
import com.app.mobile.domain.repository.HivesLocalRepository

class HivesLocalRepositoryImpl(private val hiveDao: HiveDao) : HivesLocalRepository {
    override suspend fun getHives(): List<HiveDomainPreview> =
        hiveDao.getHives().map { it.toDomain() }
}