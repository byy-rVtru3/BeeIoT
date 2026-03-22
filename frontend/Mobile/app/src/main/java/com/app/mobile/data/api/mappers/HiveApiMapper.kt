package com.app.mobile.data.api.mappers

import com.app.mobile.data.api.models.hive.HiveDetailsDto
import com.app.mobile.data.api.models.hive.HiveListItemDto
import com.app.mobile.domain.models.hives.HiveDomainPreview
import com.app.mobile.domain.models.hives.HiveResult

fun HiveListItemDto.toDomain() = HiveDomainPreview(
	name = this.name,
	sensor = this.sensor,
	hub = this.hub,
	queen = this.queen
)

fun HiveDetailsDto.toDomain() = HiveResult(
	name = this.name,
	hub = this.hub,
	queen = this.queen,
	active = this.active ?: true
)
