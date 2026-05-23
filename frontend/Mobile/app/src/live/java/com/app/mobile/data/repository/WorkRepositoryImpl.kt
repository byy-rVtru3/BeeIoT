package com.app.mobile.data.repository

import com.app.mobile.data.api.AuthApiClient
import com.app.mobile.data.api.mappers.toDomain
import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.data.api.models.task.CreateTaskRequest
import com.app.mobile.data.api.models.task.DeleteTaskRequest
import com.app.mobile.data.api.models.task.UpdateTaskRequest
import com.app.mobile.data.api.safeApiCall
import com.app.mobile.domain.models.hives.WorkDomain
import com.app.mobile.domain.repository.WorkRepository

class WorkRepositoryImpl(private val authApiClient: AuthApiClient) : WorkRepository {

    override suspend fun getWorks(hiveName: String): ApiResult<List<WorkDomain>> =
        safeApiCall(
            apiCall = { authApiClient.getTasks(hiveName) },
            onSuccess = { response -> response.data?.map { it.toDomain() } ?: emptyList() }
        )

    override suspend fun getWork(workId: String): ApiResult<WorkDomain?> =
        safeApiCall(
            apiCall = { authApiClient.getTasks() },
            onSuccess = { response -> response.data?.map { it.toDomain() }?.find { it.id == workId } }
        )

    override suspend fun addWork(work: WorkDomain): ApiResult<Unit> =
        safeApiCall {
            authApiClient.createTask(
                CreateTaskRequest(
                    hiveName = work.hiveId,
                    title = work.title,
                    description = work.text.takeIf { it.isNotEmpty() }
                )
            )
        }

    override suspend fun updateWork(work: WorkDomain): ApiResult<Unit> =
        safeApiCall {
            authApiClient.updateTask(
                UpdateTaskRequest(
                    id = work.id,
                    title = work.title,
                    description = work.text.takeIf { it.isNotEmpty() }
                )
            )
        }

    override suspend fun deleteWork(workId: String): ApiResult<Unit> =
        safeApiCall { authApiClient.deleteTask(DeleteTaskRequest(id = workId)) }
}
