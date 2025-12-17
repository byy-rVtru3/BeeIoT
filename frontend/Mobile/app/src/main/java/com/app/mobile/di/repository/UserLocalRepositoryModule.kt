package com.app.mobile.di.repository

import com.app.mobile.data.repository.UserMockRepositoryImpl
import com.app.mobile.domain.repository.UserLocalRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module


val userLocalRepositoryModule = module {
    singleOf(::UserMockRepositoryImpl) bind UserLocalRepository::class
}