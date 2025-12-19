package com.app.mobile.di.screens

import com.app.mobile.domain.usecase.hives.works.GetWorksUseCase
import com.app.mobile.presentation.ui.screens.works.list.viewmodel.WorksListViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module


val workListModule = module {

    factoryOf(::GetWorksUseCase)

    viewModelOf(::WorksListViewModel)
}