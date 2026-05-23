package com.app.mobile.data.api.mappers

import com.app.mobile.data.api.models.telemetry.LastSensorReadingDto
import com.app.mobile.data.api.models.telemetry.TelemetryDataPointDto
import com.app.mobile.domain.models.telemetry.SensorReadings
import com.app.mobile.domain.models.telemetry.TelemetryDataPoint

fun LastSensorReadingDto.toDomain() = SensorReadings(
    temperature = if (temperature == -1.0) null else temperature,
    temperatureTime = if (temperature == -1.0) null else temperatureTime,
    noise = if (noise == -1.0) null else noise,
    noiseTime = if (noise == -1.0) null else noiseTime,
    weight = if (weight == -1.0) null else weight,
    weightTime = if (weight == -1.0) null else weightTime
)

fun TelemetryDataPointDto.toDomain() = TelemetryDataPoint(
    time = time,
    value = value
)
