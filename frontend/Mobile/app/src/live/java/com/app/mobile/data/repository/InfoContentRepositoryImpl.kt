package com.app.mobile.data.repository

import com.app.mobile.data.api.PublicApiClient
import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.data.api.models.instructions.InstructionItemDto
import com.app.mobile.data.api.safeApiCall
import com.app.mobile.data.content.InfoContentCacheStore
import com.app.mobile.data.content.InfoContentDefaultsProvider
import com.app.mobile.domain.models.info.InfoContentDomain
import com.app.mobile.domain.models.info.InstructionSectionDomain
import com.app.mobile.domain.repository.InfoContentRepository

class InfoContentRepositoryImpl(
    private val publicApiClient: PublicApiClient,
    private val cacheStore: InfoContentCacheStore,
    private val defaultsProvider: InfoContentDefaultsProvider
) : InfoContentRepository {

    override suspend fun getLocalContent(): InfoContentDomain {
        return cacheStore.getContent(defaultsProvider.getDefaultContent())
    }

    override suspend fun syncContentIfNeeded(force: Boolean): ApiResult<InfoContentDomain> {
        val localContent = getLocalContent()
        if (!force && !shouldRefresh()) {
            return ApiResult.Success(localContent)
        }

        val aboutTextResult = fetchAboutText(localContent.aboutText)
        if (aboutTextResult is ApiResult.NetworkError) return aboutTextResult
        if (aboutTextResult is ApiResult.UnexpectedError) return aboutTextResult

        val sectionsResult = fetchInstructionSections(localContent.howToSections)
        if (sectionsResult is ApiResult.NetworkError) return sectionsResult
        if (sectionsResult is ApiResult.UnexpectedError) return sectionsResult

        val aboutText = (aboutTextResult as ApiResult.Success).data
        val howToSections = (sectionsResult as ApiResult.Success).data

        val mergedContent = InfoContentDomain(
            aboutText = aboutText,
            howToSections = howToSections
        )
        cacheStore.saveContent(mergedContent)
        return ApiResult.Success(mergedContent)
    }

    private suspend fun fetchAboutText(fallback: String): ApiResult<String> {
        return when (
            val result = safeApiCall(
                apiCall = { publicApiClient.getAppDescription() },
                onSuccess = { it.data?.full?.trim() }
            )
        ) {
            is ApiResult.Success -> {
                val text = result.data?.takeIf { it.isNotBlank() } ?: fallback
                cacheStore.saveLastSyncAtMillis()
                ApiResult.Success(text)
            }
            is ApiResult.HttpError -> {
                cacheStore.saveLastSyncAtMillis()
                ApiResult.Success(fallback)
            }
            is ApiResult.NetworkError -> result
            is ApiResult.UnexpectedError -> result
        }
    }

    private suspend fun fetchInstructionSections(
        fallback: List<InstructionSectionDomain>
    ): ApiResult<List<InstructionSectionDomain>> {
        return when (
            val result = safeApiCall(
                apiCall = { publicApiClient.getInstructionItems() },
                onSuccess = { it.data.orEmpty() }
            )
        ) {
            is ApiResult.Success -> {
                val sections = result.data
                    .mapNotNull(::sanitizeItem)
                    .sortedBy { it.position }
                    .map { item ->
                        InstructionSectionDomain(
                            title = item.title,
                            body = item.body,
                            showStepNumbers = item.numbered
                        )
                    }
                    .ifEmpty { fallback }
                cacheStore.saveLastSyncAtMillis()
                ApiResult.Success(sections)
            }
            is ApiResult.HttpError -> {
                cacheStore.saveLastSyncAtMillis()
                ApiResult.Success(fallback)
            }
            is ApiResult.NetworkError -> result
            is ApiResult.UnexpectedError -> result
        }
    }

    private fun sanitizeItem(item: InstructionItemDto): InstructionItemDto? {
        val title = item.title.trim()
        val body = item.body.trim()
        if (title.isBlank() || body.isBlank()) return null
        return item.copy(title = title, body = body)
    }

    private suspend fun shouldRefresh(): Boolean {
        if (!cacheStore.isCacheVersionValid()) return true
        val lastSyncAt = cacheStore.getLastSyncAtMillis()
        if (lastSyncAt <= 0L) return true
        return System.currentTimeMillis() - lastSyncAt >= SYNC_INTERVAL_MS
    }

    private companion object {
        private const val SYNC_INTERVAL_MS = 5 * 60 * 1000L
    }
}
