package com.app.mobile.di

import org.koin.core.module.Module

/**
 * Сетевые модули для develop версии
 * В develop версии используются моки, поэтому сетевые модули не нужны
 */
val networkModules: List<Module> = emptyList()

