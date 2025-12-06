package com.app.mobile.di.screens

import com.app.mobile.domain.usecase.ConfirmationUserUseCase
import com.app.mobile.domain.usecase.ValidateConfirmationFormUseCase
import com.app.mobile.presentation.ui.screens.confirmation.viewmodel.ConfirmationViewModel
import com.app.mobile.presentation.validators.ConfirmationValidator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val confirmationModule = module {

    factoryOf(::ConfirmationUserUseCase)
    factoryOf(::ValidateConfirmationFormUseCase)

    factoryOf(::ConfirmationValidator)

    single<CoroutineDispatcher> { Dispatchers.IO }

    viewModelOf(::ConfirmationViewModel)
}