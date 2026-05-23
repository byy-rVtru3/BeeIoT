package com.app.mobile.domain.usecase.theme

import com.app.mobile.domain.repository.ThemeRepository

class SetThemeUseCase(
	private val themeRepository: ThemeRepository
) : suspend (Boolean) -> Unit by themeRepository::setDarkTheme

