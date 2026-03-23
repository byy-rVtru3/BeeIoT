package com.app.mobile.domain.repository.datasource

import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.domain.models.hives.HubDomain

interface HubDataSource {
    suspend fun getHubs(): ApiResult<List<HubDomain>>
    suspend fun getHub(id: String): ApiResult<HubDomain>
    suspend fun createHub(id: String, name: String): ApiResult<Unit>
    suspend fun updateHub(id: String, name: String? = null): ApiResult<Unit>
    suspend fun deleteHub(id: String): ApiResult<Unit>
    suspend fun getHubWithSensors(id: String): ApiResult<HubDomain>
    suspend fun getHubsWithSensors(): ApiResult<List<HubDomain>>
}
