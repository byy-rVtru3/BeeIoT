package com.app.mobile.data.repository

import com.app.mobile.data.database.dao.HubDao
import com.app.mobile.domain.mappers.toDomain
import com.app.mobile.domain.mappers.toEntity
import com.app.mobile.domain.models.hives.HubDomain
import com.app.mobile.domain.repository.HubLocalRepository

class HubLocalRepositorImpl(private val hubDao: HubDao) : HubLocalRepository {
    override suspend fun saveHub(hub: HubDomain) = hubDao.saveHub(hub.toEntity())

    override suspend fun getHubs() = hubDao.getHubs().map { it.toDomain() }

    override suspend fun getHubById(hubId: String) = hubDao.getHubById(hubId)

    override suspend fun addHiveToHub(hubId: String, hiveId: String) =
        hubDao.addHiveToHub(hubId, hiveId)
}