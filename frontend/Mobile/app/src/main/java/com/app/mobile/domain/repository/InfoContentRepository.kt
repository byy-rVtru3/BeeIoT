package com.app.mobile.domain.repository

import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.domain.models.info.InfoContentDomain

interface InfoContentRepository {
    suspend fun getLocalContent(): InfoContentDomain
    suspend fun syncContentIfNeeded(force: Boolean = false): ApiResult<InfoContentDomain>
}
