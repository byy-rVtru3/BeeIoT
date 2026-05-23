package com.app.mobile.data.repository

import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.data.content.InfoContentCacheStore
import com.app.mobile.data.content.InfoContentDefaultsProvider
import com.app.mobile.domain.models.info.InfoContentDomain
import com.app.mobile.domain.repository.InfoContentRepository

class InfoContentRepositoryImpl(
    private val cacheStore: InfoContentCacheStore,
    private val defaultsProvider: InfoContentDefaultsProvider
) : InfoContentRepository {

    override suspend fun getLocalContent(): InfoContentDomain {
        return cacheStore.getContent(defaultsProvider.getDefaultContent())
    }

    override suspend fun syncContentIfNeeded(force: Boolean): ApiResult<InfoContentDomain> {
        val localContent = getLocalContent()
        if (force || cacheStore.getLastSyncAtMillis() <= 0L) {
            cacheStore.saveContent(localContent)
        }
        return ApiResult.Success(localContent)
    }
}
