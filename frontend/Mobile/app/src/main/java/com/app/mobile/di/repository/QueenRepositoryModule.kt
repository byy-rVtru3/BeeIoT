package com.app.mobile.di.repository

import com.app.mobile.data.repository.QueenRepositoryImpl
import com.app.mobile.domain.repository.QueenRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val queenRepositoryModule = module {
    singleOf(::QueenRepositoryImpl) bind QueenRepository::class
}
