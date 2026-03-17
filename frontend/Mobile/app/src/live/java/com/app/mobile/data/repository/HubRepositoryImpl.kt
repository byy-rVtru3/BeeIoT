package com.app.mobile.data.repository

import com.app.mobile.data.api.AuthApiClient
import com.app.mobile.data.api.mappers.toDomain
import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.data.api.models.hub.CreateHubRequest
import com.app.mobile.data.api.models.hub.UpdateHubRequest
import com.app.mobile.data.api.safeApiCall
import com.app.mobile.domain.models.hives.HubDomain
import com.app.mobile.domain.repository.HubRepository

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
}
