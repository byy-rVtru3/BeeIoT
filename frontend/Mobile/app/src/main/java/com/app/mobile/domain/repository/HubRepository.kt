package com.app.mobile.domain.repository

import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.domain.models.hives.HubDomain

interface HubRepository {
    suspend fun getHubs(): ApiResult<List<HubDomain>>
    suspend fun getHub(id: String): ApiResult<HubDomain>
    suspend fun createHub(id: String, name: String): ApiResult<Unit>
    suspend fun updateHub(id: String, name: String? = null): ApiResult<Unit>
}
