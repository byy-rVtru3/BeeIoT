package com.app.mobile.di.repository

import com.app.mobile.data.repository.WorkRepositoryImpl
import com.app.mobile.domain.repository.WorkRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val workRepositoryModule = module {
    singleOf(::WorkRepositoryImpl) bind WorkRepository::class
}
