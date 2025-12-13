package com.app.mobile.di

/**
 * Сетевые модули для live версии
 * Включают networkModule, publicApiModule, authApiModule для работы с реальным API
 */
val networkModules: List<Module> = listOf(
    networkModule,
    publicApiModule,
    authApiModule
)

