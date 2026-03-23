package com.app.mobile.di.repository

import com.app.mobile.data.repository.HivesDataSourceImpl
import com.app.mobile.domain.repository.datasource.HivesDataSource
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val hivesDataSourceModule = module {
    singleOf(::HivesDataSourceImpl) bind HivesDataSource::class
}
