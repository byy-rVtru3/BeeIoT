package com.app.mobile.di

import com.app.mobile.MainViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val mainModule = module {
	viewModelOf(::MainViewModel)
}