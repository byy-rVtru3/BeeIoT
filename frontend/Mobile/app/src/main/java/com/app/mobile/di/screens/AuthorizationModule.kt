package com.app.mobile.di.screens

import com.app.mobile.domain.usecase.AuthorizationAccountUseCase
import com.app.mobile.presentation.ui.screens.authorization.viewmodel.AuthorizationViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module


val authorizationModule = module {
    factoryOf(::AuthorizationAccountUseCase)

    viewModelOf(::AuthorizationViewModel)
}