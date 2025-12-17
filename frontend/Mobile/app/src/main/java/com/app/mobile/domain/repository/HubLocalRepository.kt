package com.app.mobile.domain.repository

import com.app.mobile.data.database.entity.HubEntity
import com.app.mobile.domain.models.hives.HubDomain

interface HubLocalRepository {

    suspend fun saveHub(hub: HubDomain)

    suspend fun getHubs(): List<HubDomain>

    suspend fun getHubById(hubId: String): HubEntity?

    suspend fun addHiveToHub(hubId: String, hiveId: String)

}