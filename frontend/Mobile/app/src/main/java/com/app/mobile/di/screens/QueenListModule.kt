package com.app.mobile.di.screens

import com.app.mobile.domain.usecase.hives.hive.GetHivePreviewUseCase
import com.app.mobile.domain.usecase.hives.queen.GetQueensUseCase
import com.app.mobile.presentation.ui.screens.queen.list.viewmodel.QueenListViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val queenListModule = module {

    factoryOf(::GetQueensUseCase)
    factoryOf(::GetHivePreviewUseCase)

    viewModelOf(::QueenListViewModel)
}