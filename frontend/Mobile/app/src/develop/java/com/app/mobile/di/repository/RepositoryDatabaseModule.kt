package com.app.mobile.di.repository

import com.app.mobile.data.repository.UserMockRepositoryImpl
import com.app.mobile.domain.repository.UserLocalRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module

val repositoryDatabaseModule = module {
    single { UserMockRepositoryImpl(androidContext()) } bind UserLocalRepository::class
}

