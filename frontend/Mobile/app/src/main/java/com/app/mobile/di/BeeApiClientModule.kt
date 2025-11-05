package com.app.mobile.di

import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

private const val BASE_URL = "http://84.237.53.140/api/"
private const val CONNECT_TIMEOUT = 10L
private const val WRITE_TIMEOUT = 10L
private const val READ_TIMEOUT = 10L

val networkModule = module {
    single {
        Json.asConverterFactory("application/json; charset=UTF8".toMediaType())
    }

    single {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    single {
        OkHttpClient.Builder().apply {
            getAll<Interceptor>().forEach {
                addInterceptor(it)
            }
            connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
        }.build()
    }

    single {
        Retrofit.Builder().apply {
            client(get())
            baseUrl(BASE_URL)
            addConverterFactory(get())
        }.build()
    }

}