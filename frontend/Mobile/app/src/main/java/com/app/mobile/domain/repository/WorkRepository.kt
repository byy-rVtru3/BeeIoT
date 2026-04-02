package com.app.mobile.domain.repository

import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.domain.models.hives.WorkDomain

interface WorkRepository {
    suspend fun getWork(workId: String): ApiResult<WorkDomain?>
    suspend fun getWorks(hiveName: String): ApiResult<List<WorkDomain>>
    suspend fun addWork(work: WorkDomain): ApiResult<Unit>
    suspend fun updateWork(work: WorkDomain): ApiResult<Unit>
    suspend fun deleteWork(workId: String): ApiResult<Unit>
}
