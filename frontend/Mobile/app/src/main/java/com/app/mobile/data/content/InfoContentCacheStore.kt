package com.app.mobile.data.content

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.app.mobile.domain.models.info.InfoContentDomain
import com.app.mobile.domain.models.info.InstructionSectionDomain
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class InfoContentCacheStore(
    private val dataStore: DataStore<Preferences>,
    private val json: Json
) {

    suspend fun getContent(defaultContent: InfoContentDomain): InfoContentDomain {
        val preferences = dataStore.data.first()

        val aboutText = preferences[ABOUT_TEXT_KEY]
            ?.takeIf { it.isNotBlank() }
            ?: defaultContent.aboutText

        val cachedSectionsRaw = preferences[HOW_TO_SECTIONS_KEY]
        val cachedSections = cachedSectionsRaw?.let(::decodeSections).orEmpty()
        val sections = cachedSections.ifEmpty { defaultContent.howToSections }

        return InfoContentDomain(
            aboutText = aboutText,
            howToSections = sections
        )
    }

    suspend fun getLastSyncAtMillis(): Long {
        val preferences = dataStore.data.first()
        return preferences[LAST_SYNC_AT_KEY] ?: 0L
    }

    suspend fun saveContent(content: InfoContentDomain, syncedAtMillis: Long = System.currentTimeMillis()) {
        dataStore.edit { preferences ->
            preferences[ABOUT_TEXT_KEY] = content.aboutText
            preferences[HOW_TO_SECTIONS_KEY] = encodeSections(content.howToSections)
            preferences[LAST_SYNC_AT_KEY] = syncedAtMillis
        }
    }

    suspend fun saveLastSyncAtMillis(syncedAtMillis: Long = System.currentTimeMillis()) {
        dataStore.edit { preferences ->
            preferences[LAST_SYNC_AT_KEY] = syncedAtMillis
        }
    }

    private fun encodeSections(sections: List<InstructionSectionDomain>): String {
        val cacheItems = sections.map {
            CachedInstructionSection(
                title = it.title,
                body = it.body,
                showStepNumbers = it.showStepNumbers
            )
        }

        return json.encodeToString(cacheItems)
    }

    private fun decodeSections(raw: String): List<InstructionSectionDomain> {
        val cacheItems = runCatching {
            json.decodeFromString<List<CachedInstructionSection>>(raw)
        }.getOrDefault(emptyList())

        return cacheItems.map {
            InstructionSectionDomain(
                title = it.title,
                body = it.body,
                showStepNumbers = it.showStepNumbers
            )
        }
    }

    @Serializable
    private data class CachedInstructionSection(
        val title: String,
        val body: String,
        val showStepNumbers: Boolean
    )

    private companion object {
        private val ABOUT_TEXT_KEY = stringPreferencesKey("info_content_about_text")
        private val HOW_TO_SECTIONS_KEY = stringPreferencesKey("info_content_how_to_sections")
        private val LAST_SYNC_AT_KEY = longPreferencesKey("info_content_last_sync_at")
    }
}
