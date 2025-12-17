package com.app.mobile.di.repository

import com.app.mobile.data.repository.QueenLocalRepositoryImpl
import com.app.mobile.domain.repository.QueenLocalRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val queenLocalRepositoryModule = module {

    singleOf(::QueenLocalRepositoryImpl) bind QueenLocalRepository::class

}