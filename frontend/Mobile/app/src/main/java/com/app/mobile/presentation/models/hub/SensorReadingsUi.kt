package com.app.mobile.presentation.models.hub

import com.app.mobile.presentation.models.hub.sensors.NoiseSensor
import com.app.mobile.presentation.models.hub.sensors.TemperatureSensor
import com.app.mobile.presentation.models.hub.sensors.WeightSensor

data class SensorReadingsUi(
	val temperatureSensor: TemperatureSensor?,
	val noiseSensor: NoiseSensor?,
	val weightSensor: WeightSensor?
)