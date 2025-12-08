package com.app.mobile.di.screens

import com.app.mobile.domain.usecase.ConfirmationUserUseCase
import com.app.mobile.presentation.ui.screens.confirmation.viewmodel.ConfirmationViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val confirmationModule = module {

    factoryOf(::ConfirmationUserUseCase)

    single<CoroutineDispatcher> { Dispatchers.IO }

    viewModelOf(::ConfirmationViewModel)
}