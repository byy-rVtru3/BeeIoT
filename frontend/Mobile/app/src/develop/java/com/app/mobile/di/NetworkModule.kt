package com.app.mobile.di

import android.content.Context
import com.app.mobile.data.api.interceptor.AuthInterceptor
import com.app.mobile.data.mock.MockDataSource
import com.app.mobile.data.mock.MockDataSourceImpl
import com.app.mobile.data.mock.interceptor.FakeServerInterceptor
import com.app.mobile.data.repository.AuthRepository
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

private const val REAL_BASE_URL = "http://84.237.53.140/api/"
private const val FAKE_BASE_URL = "https://fakeserver.ru/api/"
private const val CONNECT_TIMEOUT = 10L
private const val WRITE_TIMEOUT = 10L
private const val READ_TIMEOUT = 10L

val publicClient = named("publicClient")
val authorizedClient = named("authorizedClient")

val publicRetrofit = named("publicRetrofit")
val authorizedRetrofit = named("authorizedRetrofit")

val networkModule = module {

    // MockDataSource
    single<MockDataSource> { MockDataSourceImpl(get()) }

    // Converter Factory
    single {
        Json.asConverterFactory("application/json; charset=UTF8".toMediaType())
    }

    // Logging Interceptor
    single<Interceptor>(named("logging")) {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    // Auth Interceptor
    single<Interceptor>(named("auth")) {
        val authRepository: AuthRepository = get()
        AuthInterceptor {
            runBlocking {
                authRepository.getToken()
            }
        }
    }

    // Fake Server Interceptor
    single<Interceptor>(named("fake")) {
        FakeServerInterceptor(get<Context>())
    }

    // Real Public OkHttpClient
    single(named("realPublic")) {
        OkHttpClient.Builder().apply {
            addInterceptor(get<Interceptor>(named("logging")))
            connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
        }.build()
    }

    // Real Authorized OkHttpClient
    single(named("realAuthorized")) {
        OkHttpClient.Builder().apply {
            addInterceptor(get<Interceptor>(named("logging")))
            addInterceptor(get<Interceptor>(named("auth")))
            connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
        }.build()
    }

    // Fake Public OkHttpClient
    single(named("fakePublic")) {
        OkHttpClient.Builder().apply {
            addInterceptor(get<Interceptor>(named("fake")))
            addInterceptor(get<Interceptor>(named("logging")))
            connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
        }.build()
    }

    // Fake Authorized OkHttpClient
    single(named("fakeAuthorized")) {
        OkHttpClient.Builder().apply {
            addInterceptor(get<Interceptor>(named("fake")))
            addInterceptor(get<Interceptor>(named("logging")))
            addInterceptor(get<Interceptor>(named("auth")))
            connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
        }.build()
    }

    // Public OkHttpClient (выбирается в зависимости от MockDataSource)
    single(publicClient) {
        val mockDataSource = get<MockDataSource>()
        if (mockDataSource.isMock()) {
            get<OkHttpClient>(named("fakePublic"))
        } else {
            get<OkHttpClient>(named("realPublic"))
        }
    }

    // Authorized OkHttpClient (выбирается в зависимости от MockDataSource)
    single(authorizedClient) {
        val mockDataSource = get<MockDataSource>()
        if (mockDataSource.isMock()) {
            get<OkHttpClient>(named("fakeAuthorized"))
        } else {
            get<OkHttpClient>(named("realAuthorized"))
        }
    }

    // Public Retrofit
    single(publicRetrofit) {
        val mockDataSource = get<MockDataSource>()
        val baseUrl = if (mockDataSource.isMock()) FAKE_BASE_URL else REAL_BASE_URL

        Retrofit.Builder().apply {
            client(get(publicClient))
            baseUrl(baseUrl)
            addConverterFactory(get())
        }.build()
    }

    // Authorized Retrofit
    single(authorizedRetrofit) {
        val mockDataSource = get<MockDataSource>()
        val baseUrl = if (mockDataSource.isMock()) FAKE_BASE_URL else REAL_BASE_URL

        Retrofit.Builder().apply {
            client(get(authorizedClient))
            baseUrl(baseUrl)
            addConverterFactory(get())
        }.build()
    }
}
