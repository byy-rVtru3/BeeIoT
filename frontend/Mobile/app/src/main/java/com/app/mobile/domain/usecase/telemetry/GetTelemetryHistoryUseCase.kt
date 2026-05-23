package com.app.mobile.domain.usecase.telemetry

import com.app.mobile.domain.models.telemetry.SensorType
import com.app.mobile.domain.repository.datasource.TelemetryDataSource

class GetTelemetryHistoryUseCase(
	private val telemetryDataSource: TelemetryDataSource
) {
	suspend operator fun invoke(
		hubId: String,
		sensorType: SensorType,
		since: Long? = null
	) = telemetryDataSource.getTelemetryHistory(hubId, sensorType, since)
}
