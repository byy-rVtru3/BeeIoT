package com.app.mobile.di.screens

import com.app.mobile.domain.usecase.hives.works.DeleteWorkUseCase
import com.app.mobile.domain.usecase.hives.works.GetWorkUseCase
import com.app.mobile.presentation.ui.screens.works.detail.viewmodel.WorkDetailViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val workDetailModule = module {
    factoryOf(::GetWorkUseCase)
    factoryOf(::DeleteWorkUseCase)
    viewModelOf(::WorkDetailViewModel)
}
