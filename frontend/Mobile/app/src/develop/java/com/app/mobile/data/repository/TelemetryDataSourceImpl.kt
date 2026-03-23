package com.app.mobile.data.repository

import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.domain.models.telemetry.SensorType
import com.app.mobile.domain.models.telemetry.TelemetryDataPoint
import com.app.mobile.domain.repository.datasource.TelemetryDataSource
import kotlinx.coroutines.delay
import kotlin.random.Random

class TelemetryDataSourceImpl : TelemetryDataSource {

	override suspend fun getTelemetryHistory(
		hubId: String,
		sensorType: SensorType,
		since: Long?
	): ApiResult<List<TelemetryDataPoint>> {
		delay(200)
		val now = System.currentTimeMillis() / 1000
		val baseValue = when (sensorType) {
			SensorType.TEMPERATURE -> 25.0
			SensorType.NOISE -> 40.0
			SensorType.WEIGHT -> 50.0
		}
		val range = when (sensorType) {
			SensorType.TEMPERATURE -> 10.0
			SensorType.NOISE -> 20.0
			SensorType.WEIGHT -> 5.0
		}
		val mockData = (0..23).map { i ->
			TelemetryDataPoint(
				time = now - (23 - i) * 3600,
				value = baseValue + Random.nextDouble(-range / 2, range / 2)
			)
		}
		return ApiResult.Success(mockData)
	}

	override suspend fun addWeightRecord(
		hubId: String,
		weight: Double,
		time: String
	): ApiResult<Unit> {
		delay(300)
		return ApiResult.Success(Unit)
	}
}
