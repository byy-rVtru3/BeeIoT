package com.app.mobile.di.repository

import com.app.mobile.data.repository.UserLocalRepositoryImpl
import com.app.mobile.domain.repository.UserLocalRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module


val userLocalRepositoryModule = module {
    singleOf(::UserLocalRepositoryImpl) bind UserLocalRepository::class
}