package com.app.mobile.di.screens

import com.app.mobile.domain.usecase.AuthorizationAccountUseCase
import com.app.mobile.domain.usecase.ValidateAuthorizationFormUseCase
import com.app.mobile.presentation.ui.screens.authorization.viewmodel.AuthorizationViewModel
import com.app.mobile.presentation.validators.AuthorizationValidator
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module


val authorizationModule = module {
    factoryOf(::AuthorizationAccountUseCase)
    factoryOf(::AuthorizationValidator)
    factoryOf(::ValidateAuthorizationFormUseCase)

    viewModelOf(::AuthorizationViewModel)
}