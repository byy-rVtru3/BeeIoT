package com.app.mobile.di.screens

import com.app.mobile.domain.usecase.hives.GetHivePreviewUseCase
import com.app.mobile.domain.usecase.hives.GetQueenUseCase
import com.app.mobile.presentation.ui.screens.queen.queen.viewmodel.QueenViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val queenModule = module {
    factoryOf(::GetQueenUseCase)
    factoryOf(::GetHivePreviewUseCase)

    viewModelOf(::QueenViewModel)
}