package com.app.mobile.di.screens

import com.app.mobile.domain.usecase.hives.queen.CreateQueenUseCase
import com.app.mobile.domain.usecase.hives.queen.GetQueenUseCase
import com.app.mobile.domain.usecase.hives.queen.SaveQueenUseCase
import com.app.mobile.presentation.ui.screens.queen.editor.viewmodel.QueenEditorViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val queenEditorModule = module {

    factoryOf(::CreateQueenUseCase)
    factoryOf(::GetQueenUseCase)
    factoryOf(::SaveQueenUseCase)

    viewModelOf(::QueenEditorViewModel)
}
