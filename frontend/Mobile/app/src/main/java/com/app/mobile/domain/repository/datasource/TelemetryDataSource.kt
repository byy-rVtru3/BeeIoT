package com.app.mobile.domain.repository.datasource

import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.domain.models.telemetry.SensorType
import com.app.mobile.domain.models.telemetry.TelemetryDataPoint

interface TelemetryDataSource {
	suspend fun getTelemetryHistory(
		hubId: String,
		sensorType: SensorType,
		since: Long? = null
	): ApiResult<List<TelemetryDataPoint>>

	suspend fun addWeightRecord(
		hubId: String,
		weight: Double,
		time: String
	): ApiResult<Unit>
}
