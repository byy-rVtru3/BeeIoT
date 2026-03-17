package com.app.mobile.di.repository

import com.app.mobile.data.repository.HubRepositoryImpl
import com.app.mobile.domain.repository.HubRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val hubRepositoryModule = module {
    singleOf(::HubRepositoryImpl) bind HubRepository::class
}
