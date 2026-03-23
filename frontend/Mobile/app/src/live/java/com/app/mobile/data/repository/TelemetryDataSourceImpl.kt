package com.app.mobile.data.repository

import com.app.mobile.data.api.AuthApiClient
import com.app.mobile.data.api.mappers.toDomain
import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.data.api.models.telemetry.SetWeightRequest
import com.app.mobile.data.api.safeApiCall
import com.app.mobile.domain.models.telemetry.SensorType
import com.app.mobile.domain.models.telemetry.TelemetryDataPoint
import com.app.mobile.domain.repository.datasource.TelemetryDataSource

class TelemetryDataSourceImpl(
	private val authApiClient: AuthApiClient
) : TelemetryDataSource {

	override suspend fun getTelemetryHistory(
		hubId: String,
		sensorType: SensorType,
		since: Long?
	): ApiResult<List<TelemetryDataPoint>> = safeApiCall(
		apiCall = {
			when (sensorType) {
				SensorType.TEMPERATURE -> authApiClient.getTemperatureHistory(hubId, since)
				SensorType.NOISE -> authApiClient.getNoiseHistory(hubId, since)
				SensorType.WEIGHT -> authApiClient.getWeightHistory(hubId, since)
			}
		},
		onSuccess = { response -> response.data?.map { it.toDomain() } ?: emptyList() }
	)

	override suspend fun addWeightRecord(
		hubId: String,
		weight: Double,
		time: String
	): ApiResult<Unit> = safeApiCall(
		apiCall = { authApiClient.setWeight(SetWeightRequest(hub = hubId, time = time, weight = weight)) },
		onSuccess = { }
	)
}
