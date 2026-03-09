package com.app.mobile.ui.theme

import com.app.mobile.domain.usecase.theme.GetThemeInitialUseCase
import com.app.mobile.domain.usecase.theme.ObserveThemeUseCase
import com.app.mobile.domain.usecase.theme.SetThemeUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

data class ThemeState(
	val isDarkTheme: Boolean = false,
	val isReady: Boolean = false
)

class ThemeManager(
	private val observeThemeUseCase: ObserveThemeUseCase,
	private val setThemeUseCase: SetThemeUseCase,
	private val getThemeInitialUseCase: GetThemeInitialUseCase,
	private val scope: CoroutineScope
) {
	// Синхронно читаем начальное значение — избегаем вспышки
	private val initialDark: Boolean = runBlocking { getThemeInitialUseCase() }

	private val _themeState = MutableStateFlow(ThemeState(isDarkTheme = initialDark, isReady = true))
	val themeState: StateFlow<ThemeState> = _themeState.asStateFlow()

	init {
		scope.launch {
			observeThemeUseCase().collect { isDark ->
				_themeState.value = ThemeState(isDarkTheme = isDark, isReady = true)
			}
		}
	}

	fun toggleTheme() {
		scope.launch {
			setThemeUseCase(!_themeState.value.isDarkTheme)
		}
	}
}
