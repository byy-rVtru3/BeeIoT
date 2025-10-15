package com.app.mobile.di

import com.app.mobile.data.api.BeeApiClient
import com.app.mobile.data.repository.RepositoryImpl
import com.app.mobile.domain.repository.Repository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import retrofit2.Retrofit
import kotlin.jvm.java

val repositoryModule = module {
    single { get<Retrofit>().create(BeeApiClient::class.java) }
    singleOf(::RepositoryImpl) bind Repository::class
}