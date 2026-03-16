package com.app.mobile.di.screens

import com.app.mobile.domain.usecase.hives.hub.GetHubByIdUseCase
import com.app.mobile.presentation.ui.screens.hub.details.viewmodel.HubViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val hubModule = module {
    factoryOf(::GetHubByIdUseCase)
    viewModelOf(::HubViewModel)
}
