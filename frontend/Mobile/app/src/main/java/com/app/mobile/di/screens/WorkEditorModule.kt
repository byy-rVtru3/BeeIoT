package com.app.mobile.di.screens

import com.app.mobile.domain.usecase.hives.works.AddWorkUseCase
import com.app.mobile.domain.usecase.hives.works.CreateWorkUseCase
import com.app.mobile.domain.usecase.hives.works.GetWorkUseCase
import com.app.mobile.domain.usecase.hives.works.UpdateWorkUseCase
import com.app.mobile.presentation.ui.screens.works.editor.viewmodel.WorksEditorViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val workEditorModule = module {

    factoryOf(::GetWorkUseCase)
    factoryOf(::CreateWorkUseCase)
    factoryOf(::AddWorkUseCase)
    factoryOf(::UpdateWorkUseCase)

    viewModelOf(::WorksEditorViewModel)
}
