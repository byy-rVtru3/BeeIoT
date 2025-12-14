package com.app.mobile.di.screens

import com.app.mobile.domain.usecase.hives.GetHivesPreviewUseCase
import com.app.mobile.presentation.ui.screens.hives.list.vewmodel.HivesListViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module


val hivesListModule = module {

    factoryOf(::GetHivesPreviewUseCase)

    viewModelOf(::HivesListViewModel)
}