package com.app.mobile.domain.models.telemetry

enum class SensorType(val apiValue: String) {
	TEMPERATURE("temperature"),
	NOISE("noise"),
	WEIGHT("weight");

	companion object {
		fun fromString(value: String): SensorType =
			entries.first { it.apiValue == value }
	}
}
