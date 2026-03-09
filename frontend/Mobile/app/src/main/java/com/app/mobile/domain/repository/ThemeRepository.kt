package com.app.mobile.domain.repository

import kotlinx.coroutines.flow.Flow

interface ThemeRepository {

	fun isDarkTheme(): Flow<Boolean>

	suspend fun isDarkThemeSync(): Boolean

	suspend fun setDarkTheme(isDark: Boolean)
}

