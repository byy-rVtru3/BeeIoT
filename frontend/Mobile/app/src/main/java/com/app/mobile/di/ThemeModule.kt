package com.app.mobile.di

import com.app.mobile.data.repository.ThemeRepositoryImpl
import com.app.mobile.domain.repository.ThemeRepository
import com.app.mobile.domain.usecase.theme.GetThemeInitialUseCase
import com.app.mobile.domain.usecase.theme.ObserveThemeUseCase
import com.app.mobile.domain.usecase.theme.SetThemeUseCase
import com.app.mobile.ui.theme.ThemeManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.dsl.factoryOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val themeModule = module {

	single<ThemeRepository> {
		ThemeRepositoryImpl(dataStore = get(named("SettingsStore")))
	}

	factoryOf(::ObserveThemeUseCase)
	factoryOf(::SetThemeUseCase)
	factoryOf(::GetThemeInitialUseCase)

	single {
		ThemeManager(
			observeThemeUseCase = get(),
			setThemeUseCase = get(),
			getThemeInitialUseCase = get(),
			scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
		)
	}
}


