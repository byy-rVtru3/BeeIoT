package com.app.mobile.di

import com.app.mobile.data.api.PublicApiClient
import org.koin.dsl.module
import retrofit2.Retrofit

val publicApiModule = module {
    factory {
        get<Retrofit>(publicRetrofit).create(PublicApiClient::class.java)
    }
}
