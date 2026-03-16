package com.app.mobile.di.screens

import com.app.mobile.domain.usecase.hives.hub.GetHubByIdUseCase
import com.app.mobile.domain.usecase.hives.hub.SaveHubUseCase
import com.app.mobile.presentation.ui.screens.hub.editor.viewmodel.HubEditorViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val hubEditorModule = module {
    factoryOf(::GetHubByIdUseCase)
    factoryOf(::SaveHubUseCase)
    viewModelOf(::HubEditorViewModel)
}
