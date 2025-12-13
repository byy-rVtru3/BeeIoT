package com.app.mobile.di

import org.koin.core.module.Module

/**
 * Сетевые модули для live версии
 * Включают networkModule, publicApiModule, authApiModule для работы с реальным API
 */
val networkModules: List<Module> = listOf(
    networkModule,
    publicApiModule,
    authApiModule
)
