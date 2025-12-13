package com.app.mobile.di

import com.app.mobile.data.api.interceptor.AuthInterceptor
import com.app.mobile.data.repository.AuthRepository
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

private const val BASE_URL = "http://84.237.53.140/api/"
private const val CONNECT_TIMEOUT = 10L
private const val WRITE_TIMEOUT = 10L
private const val READ_TIMEOUT = 10L

val publicClient = named("publicClient")
val authorizedClient = named("authorizedClient")

val publicRetrofit = named("publicRetrofit")
val authorizedRetrofit = named("authorizedRetrofit")

val networkModule = module {

    single {
        Json.asConverterFactory("application/json; charset=UTF8".toMediaType())
    }

    single<HttpLoggingInterceptor> {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    single<AuthInterceptor> {
        val authRepository: AuthRepository = get()
        AuthInterceptor {
            runBlocking {
                authRepository.getToken()
            }
        }
    }

    factory(publicClient) {
        val loggingInterceptor = get<HttpLoggingInterceptor>()

        OkHttpClient.Builder().apply {
            addInterceptor(loggingInterceptor)
            connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
        }.build()
    }

    factory(authorizedClient) {
        val loggingInterceptor = get<HttpLoggingInterceptor>()
        val authInterceptor = get<AuthInterceptor>()

        OkHttpClient.Builder().apply {
            addInterceptor(loggingInterceptor)
            addInterceptor(authInterceptor)
            connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
        }.build()
    }

    factory(publicRetrofit) {
        Retrofit.Builder().apply {
            client(get(publicClient))
            baseUrl(BASE_URL)
            addConverterFactory(get())
        }.build()
    }

    factory(authorizedRetrofit) {
        Retrofit.Builder().apply {
            client(get(authorizedClient))
            baseUrl(BASE_URL)
            addConverterFactory(get())
        }.build()
    }
}
