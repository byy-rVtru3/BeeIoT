package com.app.mobile.di.repository

import com.app.mobile.data.repository.WorkLocalRepositoryImpl
import com.app.mobile.domain.repository.WorkLocalRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module


val workLocalRepositoryModule = module {
    singleOf(::WorkLocalRepositoryImpl) bind WorkLocalRepository::class
}