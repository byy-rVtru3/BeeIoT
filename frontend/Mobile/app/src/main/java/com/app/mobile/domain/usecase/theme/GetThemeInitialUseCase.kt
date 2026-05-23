package com.app.mobile.domain.usecase.theme

import com.app.mobile.domain.repository.ThemeRepository

class GetThemeInitialUseCase(
	private val themeRepository: ThemeRepository
) : suspend () -> Boolean by themeRepository::isDarkThemeSync

