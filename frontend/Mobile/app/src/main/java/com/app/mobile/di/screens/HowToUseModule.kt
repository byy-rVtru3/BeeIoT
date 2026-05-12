package com.app.mobile.di.screens

import com.app.mobile.presentation.ui.screens.howtouse.viewmodel.HowToUseViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val howToUseModule = module {

    viewModelOf(::HowToUseViewModel)
}
