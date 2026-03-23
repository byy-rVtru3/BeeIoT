package com.app.mobile.domain.repository.datasource

import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.domain.models.hives.HiveDomain
import com.app.mobile.domain.models.hives.HiveDomainPreview
import com.app.mobile.domain.models.hives.HiveResult

interface HivesDataSource {
    suspend fun getHives(): ApiResult<List<HiveDomainPreview>>
    suspend fun getHive(name: String): ApiResult<HiveResult>
    suspend fun createHive(name: String): ApiResult<Unit>
    suspend fun updateHive(oldName: String, newName: String? = null, active: Boolean? = null): ApiResult<Unit>
    suspend fun deleteHive(name: String): ApiResult<Unit>
    suspend fun linkHubToHive(hiveName: String, hubId: String): ApiResult<Unit>
    suspend fun linkQueenToHive(hiveName: String, queenName: String): ApiResult<Unit>
}
