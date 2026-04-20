package com.app.mobile.data.repository

import com.app.mobile.data.api.AuthApiClient
import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.data.api.models.instructions.InstructionItemDto
import com.app.mobile.data.api.safeApiCall
import com.app.mobile.data.content.InfoContentCacheStore
import com.app.mobile.data.content.InfoContentDefaultsProvider
import com.app.mobile.domain.models.info.InfoContentDomain
import com.app.mobile.domain.models.info.InstructionSectionDomain
import com.app.mobile.domain.repository.InfoContentRepository
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

class InfoContentRepositoryImpl(
    private val authApiClient: AuthApiClient,
    private val cacheStore: InfoContentCacheStore,
    private val defaultsProvider: InfoContentDefaultsProvider,
    private val json: Json
) : InfoContentRepository {

    override suspend fun getLocalContent(): InfoContentDomain {
        return cacheStore.getContent(defaultsProvider.getDefaultContent())
    }

    override suspend fun syncContentIfNeeded(force: Boolean): ApiResult<InfoContentDomain> {
        val localContent = getLocalContent()
        if (!force && !shouldRefresh()) {
            return ApiResult.Success(localContent)
        }

        return when (
            val result = safeApiCall(
                apiCall = { authApiClient.getInstructionsList() },
                onSuccess = ::parseInstructionItems
            )
        ) {
            is ApiResult.Success -> {
                val mergedContent = mergeRemoteContent(
                    remoteItems = result.data,
                    fallback = localContent
                )
                cacheStore.saveContent(mergedContent)
                ApiResult.Success(mergedContent)
            }

            is ApiResult.HttpError -> {
                if (result.code == HTTP_CODE_UNAUTHORIZED || result.code == HTTP_CODE_FORBIDDEN) {
                    cacheStore.saveLastSyncAtMillis()
                    ApiResult.Success(localContent)
                } else {
                    result
                }
            }

            is ApiResult.NetworkError -> ApiResult.NetworkError(result.exception)
            is ApiResult.UnexpectedError -> ApiResult.UnexpectedError(result.exception)
        }
    }

    private suspend fun shouldRefresh(): Boolean {
        val lastSyncAt = cacheStore.getLastSyncAtMillis()
        if (lastSyncAt <= 0L) {
            return true
        }

        val timeFromLastSync = System.currentTimeMillis() - lastSyncAt
        return timeFromLastSync >= SYNC_INTERVAL_MS
    }

    private fun parseInstructionItems(responseBody: JsonElement): List<InstructionItemDto> {
        val listElement = extractListElement(responseBody)
        return listElement.mapNotNull { element ->
            runCatching {
                json.decodeFromJsonElement<InstructionItemDto>(element)
            }.getOrNull()
        }
    }

    private fun extractListElement(responseBody: JsonElement): JsonArray {
        return when (responseBody) {
            is JsonArray -> responseBody
            is JsonObject -> {
                (responseBody["data"] as? JsonArray)
                    ?: (responseBody["items"] as? JsonArray)
                    ?: JsonArray(emptyList())
            }

            else -> JsonArray(emptyList())
        }
    }

    private fun mergeRemoteContent(
        remoteItems: List<InstructionItemDto>,
        fallback: InfoContentDomain
    ): InfoContentDomain {
        val sanitizedItems = remoteItems.mapNotNull(::sanitizeItem)
        if (sanitizedItems.isEmpty()) {
            return fallback
        }

        val aboutItem = sanitizedItems.firstOrNull { isAboutInstruction(it.title) }
        val aboutText = aboutItem?.content ?: fallback.aboutText

        val howToSections = sanitizedItems
            .filterNot { isAboutInstruction(it.title) }
            .sortedBy { it.id ?: Int.MAX_VALUE }
            .map {
                InstructionSectionDomain(
                    title = it.title,
                    body = it.content,
                    showStepNumbers = shouldShowStepNumbers(it.title)
                )
            }
            .ifEmpty { fallback.howToSections }

        return InfoContentDomain(
            aboutText = aboutText,
            howToSections = howToSections
        )
    }

    private fun sanitizeItem(item: InstructionItemDto): InstructionItemDto? {
        val title = item.title.trim()
        val content = item.content.trim()
        if (title.isBlank() || content.isBlank()) {
            return null
        }

        return item.copy(
            title = title,
            content = content
        )
    }

    private fun isAboutInstruction(title: String): Boolean {
        val normalizedTitle = title.lowercase()
        return normalizedTitle.contains("о приложении") || normalizedTitle.contains("about")
    }

    private fun shouldShowStepNumbers(title: String): Boolean {
        val normalizedTitle = title.lowercase()
        return !normalizedTitle.contains("навигац") && !normalizedTitle.contains("navigation")
    }

    private companion object {
        private const val SYNC_INTERVAL_MS = 15 * 60 * 1000L
        private const val HTTP_CODE_UNAUTHORIZED = 401
        private const val HTTP_CODE_FORBIDDEN = 403
    }
}
