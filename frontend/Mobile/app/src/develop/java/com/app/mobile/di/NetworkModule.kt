package com.app.mobile.di

import android.content.Context
import com.app.mobile.data.api.interceptor.AuthInterceptor
import com.app.mobile.data.mock.MockDataSourceImpl
import com.app.mobile.data.mock.interceptor.FakeServerInterceptor
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

    // MockDataSource для переключения между mock/real режимами
    single { MockDataSourceImpl(get()) }

    // JSON Converter Factory
    single {
        Json.asConverterFactory("application/json; charset=UTF8".toMediaType())
    }

    // Logging Interceptor
    single<HttpLoggingInterceptor> {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    // Auth Interceptor
    single<AuthInterceptor> {
        val authRepository: AuthRepository = get()
        AuthInterceptor {
            runBlocking {
                authRepository.getToken()
            }
        }
    }

    // Fake Server Interceptor
    single<FakeServerInterceptor> {
        FakeServerInterceptor(get<Context>())
    }

    // Public Client (без авторизации)
    factory(publicClient) {
        val mockDataSource = get<MockDataSourceImpl>()
        val loggingInterceptor = get<HttpLoggingInterceptor>()

        OkHttpClient.Builder().apply {
            addInterceptor(loggingInterceptor)
            // FakeServerInterceptor
            if (mockDataSource.isMock()) {
                addInterceptor(get<FakeServerInterceptor>())
            }
            connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
        }.build()
    }

    // Authorized Client
    factory(authorizedClient) {
        val mockDataSource = get<MockDataSourceImpl>()
        val loggingInterceptor = get<HttpLoggingInterceptor>()
        val authInterceptor = get<AuthInterceptor>()

        OkHttpClient.Builder().apply {
            addInterceptor(loggingInterceptor)
            addInterceptor(authInterceptor)
            // FakeServerInterceptor
            if (mockDataSource.isMock()) {
                addInterceptor(get<FakeServerInterceptor>())
            }
            connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
        }.build()
    }

    // Public Retrofit
    factory(publicRetrofit) {
        val mockDataSource = get<MockDataSourceImpl>()
        val baseUrl = if (mockDataSource.isMock()) FAKE_BASE_URL else REAL_BASE_URL

        Retrofit.Builder().apply {
            client(get(publicClient))
            baseUrl(baseUrl)
            addConverterFactory(get())
        }.build()
    }

    // Authorized Retrofit
    factory(authorizedRetrofit) {
        val mockDataSource = get<MockDataSourceImpl>()
        val baseUrl = if (mockDataSource.isMock()) FAKE_BASE_URL else REAL_BASE_URL

        Retrofit.Builder().apply {
            client(get(authorizedClient))
            baseUrl(baseUrl)
            addConverterFactory(get())
        }.build()
    }
}
