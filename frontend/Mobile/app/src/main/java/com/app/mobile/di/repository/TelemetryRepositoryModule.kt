package com.app.mobile.di.repository

import com.app.mobile.data.repository.TelemetryDataSourceImpl
import com.app.mobile.domain.repository.datasource.TelemetryDataSource
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val telemetryRepositoryModule = module {
	singleOf(::TelemetryDataSourceImpl) bind TelemetryDataSource::class
}
