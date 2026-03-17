package com.app.mobile.data.repository

import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.domain.models.hives.HubDomain
import com.app.mobile.domain.repository.HubRepository
import kotlinx.coroutines.delay

class HubRepositoryImpl : HubRepository {

    private val mockHubs = listOf(
        HubDomain(id = "hub-001", name = "Hub Alpha"),
        HubDomain(id = "hub-002", name = "Hub Beta")
    )

    override suspend fun getHubs(): ApiResult<List<HubDomain>> {
        delay(100)
        return ApiResult.Success(mockHubs)
    }

    override suspend fun getHub(id: String): ApiResult<HubDomain> {
        delay(100)
        val hub = mockHubs.find { it.id == id }
        return if (hub != null) {
            ApiResult.Success(hub)
        } else {
            ApiResult.HttpError(404, "Хаб не найден")
        }
    }

    override suspend fun createHub(id: String, name: String): ApiResult<Unit> {
        delay(100)
        return ApiResult.Success(Unit)
    }

    override suspend fun updateHub(id: String, name: String?): ApiResult<Unit> {
        delay(100)
        return ApiResult.Success(Unit)
    }
}
