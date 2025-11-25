package com.app.mobile.di

import com.app.mobile.data.api.interceptor.AuthInterceptor
import com.app.mobile.data.repository.AuthRepository
import com.app.mobile.data.mock.MockDataSource
import com.app.mobile.data.mock.MockDataSourceImpl
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
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

    // MockDataSource (из develop-app)
    single<MockDataSource> { MockDataSourceImpl(get()) }

    // Converter Factory
    single {
        Json.asConverterFactory("application/json; charset=UTF8".toMediaType())
    }

    // Logging Interceptor
    single {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    // AuthInterceptor (из твоей ветки)
    single<Interceptor>(named("AuthInterceptor")) {
        val authRepository: AuthRepository = get()
        AuthInterceptor {
            runBlocking {
                authRepository.getToken()
            }
        }
    }

    // PUBLIC OkHttpClient
    single(publicClient) {
        OkHttpClient.Builder().apply {
            addInterceptor(get<HttpLoggingInterceptor>())
            connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
        }.build()
    }

    // AUTHORIZED OkHttpClient (из твоей ветки)
    single(authorizedClient) {
        OkHttpClient.Builder().apply {
            addInterceptor(get<HttpLoggingInterceptor>())
            addInterceptor(get<Interceptor>(named("AuthInterceptor")))
            connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
        }.build()
    }

    // PUBLIC Retrofit
    single(publicRetrofit) {
        Retrofit.Builder().apply {
            client(get(publicClient))
            baseUrl(BASE_URL)
            addConverterFactory(get())
        }.build()
    }

    // AUTHORIZED Retrofit
    single(authorizedRetrofit) {
        Retrofit.Builder().apply {
            client(get(authorizedClient))
            baseUrl(BASE_URL)
            addConverterFactory(get())
        }.build()
    }
}
