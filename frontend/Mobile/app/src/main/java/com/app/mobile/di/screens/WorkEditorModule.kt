package com.app.mobile.di.screens

import com.app.mobile.domain.usecase.hives.works.CreateWorkUseCase
import com.app.mobile.domain.usecase.hives.works.GetWorkUseCase
import com.app.mobile.domain.usecase.hives.works.SaveWorkUseCase
import com.app.mobile.presentation.ui.screens.works.editor.viewmodel.WorksEditorViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val workEditorModule = module {

    factoryOf(::GetWorkUseCase)
    factoryOf(::CreateWorkUseCase)
    factoryOf(::SaveWorkUseCase)

    viewModelOf(::WorksEditorViewModel)
}