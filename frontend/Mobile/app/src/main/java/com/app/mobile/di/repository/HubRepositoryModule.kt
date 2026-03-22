package com.app.mobile.di.repository

import com.app.mobile.data.repository.HubDataSourceImpl
import com.app.mobile.domain.repository.datasource.HubDataSource
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val hubRepositoryModule = module {
    singleOf(::HubDataSourceImpl) bind HubDataSource::class
}
