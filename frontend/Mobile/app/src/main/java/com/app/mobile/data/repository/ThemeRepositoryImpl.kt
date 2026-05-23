package com.app.mobile.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.app.mobile.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val IS_DARK_THEME_KEY = booleanPreferencesKey("is_dark_theme")

class ThemeRepositoryImpl(
	private val dataStore: DataStore<Preferences>
) : ThemeRepository {

	override fun isDarkTheme(): Flow<Boolean> {
		return dataStore.data.map { preferences ->
			preferences[IS_DARK_THEME_KEY] ?: false
		}
	}

	override suspend fun isDarkThemeSync(): Boolean {
		return dataStore.data.map { preferences ->
			preferences[IS_DARK_THEME_KEY] ?: false
		}.first()
	}

	override suspend fun setDarkTheme(isDark: Boolean) {
		dataStore.edit { preferences ->
			preferences[IS_DARK_THEME_KEY] = isDark
		}
	}
}

