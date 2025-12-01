package com.app.mobile.di

import com.app.mobile.data.api.AuthApiClient
import org.koin.dsl.module
import retrofit2.Retrofit

val authApiModule = module {
    factory {
        get<Retrofit>(authorizedRetrofit).create(AuthApiClient::class.java)
    }
}