package com.app.mobile.di.screens

import com.app.mobile.domain.usecase.telemetry.AddWeightRecordUseCase
import com.app.mobile.domain.usecase.telemetry.GetTelemetryHistoryUseCase
import com.app.mobile.presentation.ui.screens.sensorchart.viewmodel.SensorChartViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val sensorChartModule = module {
	factoryOf(::GetTelemetryHistoryUseCase)
	factoryOf(::AddWeightRecordUseCase)
	viewModelOf(::SensorChartViewModel)
}
