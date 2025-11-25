package com.app.mobile.di.screens

import com.app.mobile.domain.usecase.DeleteAccountUseCase
import com.app.mobile.domain.usecase.GetAccountInfoUseCase
import com.app.mobile.presentation.ui.screens.accountinfo.viewmodel.AccountInfoViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val accountInfoModule = module {

    factoryOf(::GetAccountInfoUseCase)
    factoryOf(::DeleteAccountUseCase)

    viewModelOf(::AccountInfoViewModel)
}