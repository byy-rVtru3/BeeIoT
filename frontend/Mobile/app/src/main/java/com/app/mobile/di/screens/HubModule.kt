package com.app.mobile.di.screens

import com.app.mobile.domain.usecase.hives.hub.DeleteHubUseCase
import com.app.mobile.domain.usecase.hives.hub.GetHubWithSensorsUseCase
import com.app.mobile.presentation.ui.screens.hub.details.viewmodel.HubViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val hubModule = module {
    factoryOf(::GetHubWithSensorsUseCase)
    factoryOf(::DeleteHubUseCase)
    viewModelOf(::HubViewModel)
}
