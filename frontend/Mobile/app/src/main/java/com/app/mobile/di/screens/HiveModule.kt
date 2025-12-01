package com.app.mobile.di.screens

import com.app.mobile.domain.usecase.hives.GetHiveUseCase
import com.app.mobile.presentation.ui.screens.hive.viewmodel.HiveViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module


val hiveModule = module {
    factoryOf(::GetHiveUseCase)

    viewModelOf(::HiveViewModel)
}