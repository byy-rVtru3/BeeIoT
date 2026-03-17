package com.app.mobile.data.repository

import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.domain.models.hives.HiveDomain
import com.app.mobile.domain.models.hives.HiveDomainPreview
import com.app.mobile.domain.repository.HivesRepository
import kotlinx.coroutines.delay

class HivesRepositoryImpl : HivesRepository {

    private val mockHives = mutableListOf(
        HiveDomainPreview(name = "Улей-1", sensor = "sensor-001", hub = "hub-001", queen = "Матка-1"),
        HiveDomainPreview(name = "Улей-2", sensor = null, hub = null, queen = null),
        HiveDomainPreview(name = "Улей-3", sensor = "sensor-003", hub = null, queen = "Матка-2")
    )

    override suspend fun getHives(): ApiResult<List<HiveDomainPreview>> {
        delay(100)
        return ApiResult.Success(mockHives.toList())
    }

    override suspend fun getHive(name: String): ApiResult<HiveDomain> {
        delay(100)
        val hive = mockHives.find { it.name == name }
        return if (hive != null) {
            ApiResult.Success(
                HiveDomain(
                    name = hive.name,
                    sensor = hive.sensor,
                    hubName = hive.hub,
                    queenName = hive.queen,
                    active = true
                )
            )
        } else {
            ApiResult.HttpError(404, "Улей не найден")
        }
    }

    override suspend fun createHive(name: String): ApiResult<Unit> {
        delay(100)
        mockHives.add(HiveDomainPreview(name = name))
        return ApiResult.Success(Unit)
    }

    override suspend fun updateHive(oldName: String, newName: String?, active: Boolean?): ApiResult<Unit> {
        delay(100)
        return ApiResult.Success(Unit)
    }

    override suspend fun deleteHive(name: String): ApiResult<Unit> {
        delay(100)
        mockHives.removeAll { it.name == name }
        return ApiResult.Success(Unit)
    }

    override suspend fun linkHubToHive(hiveName: String, hubId: String): ApiResult<Unit> {
        delay(100)
        return ApiResult.Success(Unit)
    }

    override suspend fun linkQueenToHive(hiveName: String, queenName: String): ApiResult<Unit> {
        delay(100)
        return ApiResult.Success(Unit)
    }
}
