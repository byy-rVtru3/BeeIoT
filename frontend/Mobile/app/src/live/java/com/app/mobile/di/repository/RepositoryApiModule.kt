package com.app.mobile.di.repository

import com.app.mobile.data.repository.RepositoryApiImpl
import com.app.mobile.domain.repository.RepositoryApi
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val repositoryApiModule = module {
    factoryOf(::RepositoryApiImpl) bind RepositoryApi::class
}