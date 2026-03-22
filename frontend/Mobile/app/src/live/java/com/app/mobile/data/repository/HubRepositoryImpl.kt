package com.app.mobile.data.repository

import com.app.mobile.data.api.AuthApiClient
import com.app.mobile.data.api.mappers.toDomain
import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.data.api.models.hub.CreateHubRequest
import com.app.mobile.data.api.models.hub.UpdateHubRequest
import com.app.mobile.data.api.safeApiCall
import com.app.mobile.domain.models.hives.HubDomain
import com.app.mobile.domain.repository.HubRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class HubRepositoryImpl(
    private val authApiClient: AuthApiClient
) : HubRepository {

    override suspend fun getHubs(): ApiResult<List<HubDomain>> =
        safeApiCall(
            apiCall = { authApiClient.getHubs() },
            onSuccess = { response -> response.data?.map { it.toDomain() } ?: emptyList() }
        )

    override suspend fun getHub(id: String): ApiResult<HubDomain> =
        safeApiCall(
            apiCall = { authApiClient.getHub(id) },
            onSuccess = { response ->
                response.data?.toDomain()
                    ?: throw IllegalStateException("Hub data is null")
            }
        )

    override suspend fun createHub(id: String, name: String): ApiResult<Unit> =
        safeApiCall { authApiClient.createHub(CreateHubRequest(id = id, name = name)) }

    override suspend fun updateHub(id: String, name: String?): ApiResult<Unit> =
        safeApiCall { authApiClient.updateHub(UpdateHubRequest(id = id, name = name)) }

    override suspend fun getHubWithSensors(id: String): ApiResult<HubDomain> {
        val hubResult = getHub(id)
        if (hubResult !is ApiResult.Success) return hubResult

        val sensorResult = safeApiCall(
            apiCall = { authApiClient.getLastSensorReading(id) },
            onSuccess = { response -> response.data?.toDomain() }
        )

        val sensorReadings = (sensorResult as? ApiResult.Success)?.data

        return ApiResult.Success(hubResult.data.copy(sensorReadings = sensorReadings))
    }

    override suspend fun getHubsWithSensors(): ApiResult<List<HubDomain>> {
        val hubsResult = getHubs()
        if (hubsResult !is ApiResult.Success) return hubsResult

        val hubsWithSensors = coroutineScope {
            hubsResult.data.map { hub ->
                async {
                    val sensorResult = safeApiCall(
                        apiCall = { authApiClient.getLastSensorReading(hub.id) },
                        onSuccess = { response -> response.data?.toDomain() }
                    )
                    val sensorReadings = (sensorResult as? ApiResult.Success)?.data
                    hub.copy(sensorReadings = sensorReadings)
                }
            }.awaitAll()
        }

        return ApiResult.Success(hubsWithSensors)
    }
}
