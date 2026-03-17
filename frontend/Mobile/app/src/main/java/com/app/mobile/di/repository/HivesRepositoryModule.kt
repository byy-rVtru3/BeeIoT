package com.app.mobile.di.repository

import com.app.mobile.data.repository.HivesRepositoryImpl
import com.app.mobile.domain.repository.HivesRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val hivesRepositoryModule = module {
    singleOf(::HivesRepositoryImpl) bind HivesRepository::class
}
