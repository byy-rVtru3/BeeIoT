package com.app.mobile.di.screens

import com.app.mobile.domain.usecase.hives.hive.CreateHiveUseCase
import com.app.mobile.domain.usecase.hives.hive.GetHiveUseCase
import com.app.mobile.domain.usecase.hives.hive.SaveHiveUseCase
import com.app.mobile.domain.usecase.hives.hub.AddHiveToHubUseCase
import com.app.mobile.domain.usecase.hives.hub.GetHubsUseCase
import com.app.mobile.domain.usecase.hives.queen.AddHiveToQueenUseCase
import com.app.mobile.domain.usecase.hives.queen.GetQueensUseCase
import com.app.mobile.presentation.ui.screens.hive.editor.viewmodel.HiveEditorViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val hiveEditorModule = module {

    factoryOf(::GetHiveUseCase)
    factoryOf(::GetQueensUseCase)
    factoryOf(::GetHubsUseCase)
    factoryOf(::CreateHiveUseCase)
    factoryOf(::SaveHiveUseCase)
    factoryOf(::AddHiveToQueenUseCase)
    factoryOf(::AddHiveToHubUseCase)

    viewModelOf(::HiveEditorViewModel)
}