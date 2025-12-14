package com.app.mobile.di.repository

import com.app.mobile.data.repository.RepositoryDatabaseImpl
import com.app.mobile.domain.repository.RepositoryDatabase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val repositoryDatabaseModule = module {
    singleOf(::RepositoryDatabaseImpl) bind RepositoryDatabase::class
}

