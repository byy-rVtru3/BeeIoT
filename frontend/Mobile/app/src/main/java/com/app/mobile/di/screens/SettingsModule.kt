package com.app.mobile.di.screens

import com.app.mobile.domain.usecase.account.LogoutAccountUseCase
import com.app.mobile.presentation.ui.screens.settings.viewmodel.SettingsViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val settingsModule = module {

    factoryOf(::LogoutAccountUseCase)

    viewModelOf(::SettingsViewModel)
}