package com.app.mobile.data.repository

import com.app.mobile.data.api.AuthApiClient
import com.app.mobile.data.api.mappers.toDomain
import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.data.api.models.hive.CreateHiveRequest
import com.app.mobile.data.api.models.hive.DeleteHiveRequest
import com.app.mobile.data.api.models.hive.LinkToHiveRequest
import com.app.mobile.data.api.models.hive.UpdateHiveRequest
import com.app.mobile.data.api.safeApiCall
import com.app.mobile.domain.models.hives.HiveDomain
import com.app.mobile.domain.models.hives.HiveDomainPreview
import com.app.mobile.domain.models.hives.HiveResult
import com.app.mobile.domain.repository.datasource.HivesDataSource

class HivesDataSourceImpl(
    private val authApiClient: AuthApiClient
) : HivesDataSource {

    override suspend fun getHives(): ApiResult<List<HiveDomainPreview>> =
        safeApiCall(
            apiCall = { authApiClient.getHives() },
            onSuccess = { response -> response.data?.map { it.toDomain() } ?: emptyList() }
        )

    override suspend fun getHive(name: String): ApiResult<HiveResult> =
        safeApiCall(
            apiCall = { authApiClient.getHive(name) },
            onSuccess = { response ->
                response.data?.toDomain()
                    ?: throw IllegalStateException("Hive data is null")
            }
        )

    override suspend fun createHive(name: String): ApiResult<Unit> =
        safeApiCall { authApiClient.createHive(CreateHiveRequest(name = name)) }

    override suspend fun updateHive(oldName: String, newName: String?, active: Boolean?): ApiResult<Unit> =
        safeApiCall {
            authApiClient.updateHive(
                UpdateHiveRequest(oldName = oldName, newName = newName, active = active)
            )
        }

    override suspend fun deleteHive(name: String): ApiResult<Unit> =
        safeApiCall { authApiClient.deleteHive(DeleteHiveRequest(name = name)) }

    override suspend fun linkHubToHive(hiveName: String, hubId: String): ApiResult<Unit> =
        safeApiCall {
            authApiClient.linkHubToHive(LinkToHiveRequest(hiveName = hiveName, targetName = hubId))
        }

    override suspend fun linkQueenToHive(hiveName: String, queenName: String): ApiResult<Unit> =
        safeApiCall {
            authApiClient.linkQueenToHive(LinkToHiveRequest(hiveName = hiveName, targetName = queenName))
        }
}
