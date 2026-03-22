package com.app.mobile.data.repository

import com.app.mobile.data.api.AuthApiClient
import com.app.mobile.data.api.mappers.toDomain
import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.data.api.models.queen.CreateQueenRequest
import com.app.mobile.data.api.models.queen.DeleteQueenRequest
import com.app.mobile.data.api.models.queen.UpdateQueenRequest
import com.app.mobile.data.api.safeApiCall
import com.app.mobile.domain.models.hives.queen.QueenDomain
import com.app.mobile.domain.models.hives.queen.QueenDomainPreview
import com.app.mobile.domain.repository.datasource.QueenDataSource
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.time.LocalDate

class QueenDataSourceImpl(
    private val authApiClient: AuthApiClient
) : QueenDataSource {

    override suspend fun getQueens(): ApiResult<List<QueenDomainPreview>> =
        safeApiCall(
            apiCall = { authApiClient.getQueens() },
            onSuccess = { response -> response.data?.map { it.toDomain() } ?: emptyList() }
        )

    override suspend fun getQueen(name: String): ApiResult<QueenDomain> =
        safeApiCall(
            apiCall = { authApiClient.getQueen(name) },
            onSuccess = { response ->
                response.data?.toDomain()
                    ?: throw IllegalStateException("Queen data is null")
            }
        )

    override suspend fun createQueen(name: String, startDate: LocalDate): ApiResult<QueenDomain> =
        safeApiCall(
            apiCall = {
                authApiClient.createQueen(
                    CreateQueenRequest(name = name, startDate = startDate.toString())
                )
            },
            onSuccess = { response ->
                response.data?.toDomain()
                    ?: throw IllegalStateException("Queen data is null")
            }
        )

    override suspend fun updateQueen(oldName: String, newName: String?, startDate: String?): ApiResult<Unit> =
        safeApiCall {
            authApiClient.updateQueen(
                UpdateQueenRequest(oldName = oldName, newName = newName, startDate = startDate)
            )
        }

    override suspend fun deleteQueen(name: String): ApiResult<Unit> =
        safeApiCall { authApiClient.deleteQueen(DeleteQueenRequest(name = name)) }

    override suspend fun getQueensWithCalendars(): ApiResult<List<QueenDomain>> {
        val queensResult = getQueens()
        if (queensResult !is ApiResult.Success) {
            @Suppress("UNCHECKED_CAST")
            return queensResult as ApiResult<List<QueenDomain>>
        }

        val queensWithCalendars = coroutineScope {
            queensResult.data.map { preview ->
                async {
                    val queenResult = getQueen(preview.name)
                    (queenResult as? ApiResult.Success)?.data
                }
            }.awaitAll().filterNotNull()
        }

        return ApiResult.Success(queensWithCalendars)
    }
}
