package com.app.mobile.di.repository

import com.app.mobile.data.repository.RepositoryDatabaseImpl
import com.app.mobile.domain.repository.RepositoryDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module

val repositoryDatabaseModule = module {
    single { RepositoryDatabaseImpl(androidContext()) } bind RepositoryDatabase::class
}

