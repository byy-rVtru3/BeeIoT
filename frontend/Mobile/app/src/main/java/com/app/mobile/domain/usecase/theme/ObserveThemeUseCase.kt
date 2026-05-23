package com.app.mobile.domain.usecase.theme

import com.app.mobile.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.Flow

class ObserveThemeUseCase(
	private val themeRepository: ThemeRepository
) : () -> Flow<Boolean> by themeRepository::isDarkTheme

