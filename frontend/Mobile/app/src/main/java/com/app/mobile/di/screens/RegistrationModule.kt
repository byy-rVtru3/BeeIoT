package com.app.mobile.di.screens

import com.app.mobile.domain.usecase.CreateUserAccountUseCase
import com.app.mobile.domain.usecase.RegistrationAccountUseCase
import com.app.mobile.presentation.ui.screens.registration.viewmodel.RegistrationViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module


val registrationModule = module {

    factoryOf(::RegistrationAccountUseCase)
    factoryOf(::CreateUserAccountUseCase)

    single<CoroutineDispatcher> { Dispatchers.IO }

    viewModelOf(::RegistrationViewModel)
}