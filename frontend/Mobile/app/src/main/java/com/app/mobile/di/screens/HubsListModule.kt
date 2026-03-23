package com.app.mobile.di.screens

import com.app.mobile.domain.usecase.hives.hub.GetHubsUseCase
import com.app.mobile.presentation.ui.screens.hub.list.viewmodel.HubsListViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val hubsListModule = module {
    factoryOf(::GetHubsUseCase)
    viewModelOf(::HubsListViewModel)
}
