package com.app.mobile.di.screens

import com.app.mobile.presentation.ui.screens.aboutapp.viewmodel.AboutAppViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val aboutAppModule = module {

    viewModelOf(::AboutAppViewModel)
}