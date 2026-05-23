package com.app.mobile.di.repository

import com.app.mobile.data.repository.QueenDataSourceImpl
import com.app.mobile.domain.repository.datasource.QueenDataSource
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val queenDataSourceModule = module {
    singleOf(::QueenDataSourceImpl) bind QueenDataSource::class
}
