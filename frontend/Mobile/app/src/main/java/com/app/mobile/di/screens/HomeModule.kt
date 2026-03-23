package com.app.mobile.di.screens

import com.app.mobile.domain.usecase.hives.hive.GetHivesPreviewUseCase
import com.app.mobile.domain.usecase.hives.hub.GetHubsUseCase
import com.app.mobile.domain.usecase.hives.queen.GetQueensUseCase
import com.app.mobile.domain.usecase.hives.works.GetWorksUseCase
import com.app.mobile.presentation.ui.screens.main.viewmodel.HomeViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val homeModule = module {
    factoryOf(::GetHivesPreviewUseCase)
    factoryOf(::GetQueensUseCase)
    factoryOf(::GetHubsUseCase)
    factoryOf(::GetWorksUseCase)
    viewModelOf(::HomeViewModel)
}
