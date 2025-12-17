package com.app.mobile.di.repository

import com.app.mobile.data.repository.HivesLocalRepositoryImpl
import com.app.mobile.domain.repository.HivesLocalRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module


val hivesLocalRepositoryModule = module {
    singleOf(::HivesLocalRepositoryImpl) bind HivesLocalRepository::class
}