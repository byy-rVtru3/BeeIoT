package com.app.mobile.data.repository

import com.app.mobile.data.database.dao.HiveDao
import com.app.mobile.domain.mappers.toDomain
import com.app.mobile.domain.mappers.toEntity
import com.app.mobile.domain.models.hives.HiveDomainPreview
import com.app.mobile.domain.models.hives.HiveEditorDomain
import com.app.mobile.domain.repository.HivesLocalRepository

class HivesLocalRepositoryImpl(private val hiveDao: HiveDao) : HivesLocalRepository {
    override suspend fun getHives(): List<HiveDomainPreview> =
        hiveDao.getHives().map { it.toDomain() }

    override suspend fun getHive(hiveId: String) =
        hiveDao.getHive(hiveId)?.toDomain()

    override suspend fun getHivePreview(hiveId: String) =
        hiveDao.getHivePreview(hiveId)?.toDomain()

    override suspend fun saveHive(hive: HiveEditorDomain) {
        hiveDao.saveHive(hive.toEntity())
    }
}