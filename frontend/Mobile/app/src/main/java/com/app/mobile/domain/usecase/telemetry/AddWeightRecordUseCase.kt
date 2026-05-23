package com.app.mobile.domain.usecase.telemetry

import com.app.mobile.domain.repository.datasource.TelemetryDataSource

class AddWeightRecordUseCase(
	private val telemetryDataSource: TelemetryDataSource
) {
	suspend operator fun invoke(hubId: String, weight: Double, time: String) =
		telemetryDataSource.addWeightRecord(hubId, weight, time)
}
