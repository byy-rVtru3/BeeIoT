package com.app.mobile.di.screens

import com.app.mobile.domain.usecase.hives.GetHivesPreviewUseCase
import com.app.mobile.domain.usecase.hives.queen.CreateQueenUseCase
import com.app.mobile.domain.usecase.hives.queen.SaveQueenUseCase
import com.app.mobile.presentation.ui.screens.queen.add.viewmodel.QueenAddViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val queenAddModule = module {

    factoryOf(::CreateQueenUseCase)
    factoryOf(::GetHivesPreviewUseCase)
    factoryOf(::SaveQueenUseCase)

    viewModelOf(::QueenAddViewModel)
}