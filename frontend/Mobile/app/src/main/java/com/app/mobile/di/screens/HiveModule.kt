package com.app.mobile.di.screens

import com.app.mobile.domain.usecase.hives.hive.GetHiveUseCase
import com.app.mobile.presentation.ui.screens.hive.details.viewmodel.HiveViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module


val hiveModule = module {
    factoryOf(::GetHiveUseCase)

    viewModelOf(::HiveViewModel)
}