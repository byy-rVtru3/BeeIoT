package com.app.mobile.di

import kotlinx.serialization.json.Json
import org.koin.dsl.module

val jsonModule = module {

    single<Json> {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true
        }
    }

}