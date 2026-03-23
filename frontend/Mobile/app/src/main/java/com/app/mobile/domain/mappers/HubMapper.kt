package com.app.mobile.domain.mappers

import com.app.mobile.core.extensions.toDisplayDate
import com.app.mobile.domain.models.hives.HubDomain
import com.app.mobile.domain.models.telemetry.SensorReadings
import com.app.mobile.presentation.models.hive.HubPreviewModel
import com.app.mobile.presentation.models.hub.HubDetailUi
import com.app.mobile.presentation.models.hub.HubModel
import com.app.mobile.presentation.models.hub.SensorReadingsUi
import com.app.mobile.presentation.models.hub.sensors.NoiseSensor
import com.app.mobile.presentation.models.hub.sensors.TemperatureSensor
import com.app.mobile.presentation.models.hub.sensors.WeightSensor

fun HubDomain.toUiModel() = HubDetailUi(
	id = this.id,
	name = this.name,
	sensorReadings = this.sensorReadings?.toUiModel()
)

fun SensorReadings.toUiModel(): SensorReadingsUi {
	val tempSensor = if (temperature != null && temperatureTime != null) {
		TemperatureSensor(temperature, temperatureTime.toDisplayDate())
	} else null

	val noiseSensor = if (noise != null && noiseTime != null) {
		NoiseSensor(noise, noiseTime.toDisplayDate())
	} else null

	val weightSensor = if (weight != null && weightTime != null) {
		WeightSensor(weight, weightTime.toDisplayDate())
	} else null

	return SensorReadingsUi(
		temperatureSensor = tempSensor,
		noiseSensor = noiseSensor,
		weightSensor = weightSensor
	)
}

fun HubDomain.toPreviewModel() = HubPreviewModel(
	id = this.id,
	name = this.name
)

fun HubDomain.toEditorModel() = HubModel(
	id = this.id,
	name = this.name,
)

