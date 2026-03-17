package com.app.mobile.data.api.mappers

import com.app.mobile.data.api.models.hive.HiveDetailsDto
import com.app.mobile.data.api.models.hive.HiveListItemDto
import com.app.mobile.domain.models.hives.HiveDomain
import com.app.mobile.domain.models.hives.HiveDomainPreview

fun HiveListItemDto.toDomain() = HiveDomainPreview(
    name = this.name,
    sensor = this.sensor,
    hub = this.hub,
    queen = this.queen
)

fun HiveDetailsDto.toDomain() = HiveDomain(
    name = this.name,
    sensor = this.sensor,
    hubName = this.hub,
    queenName = this.queen,
    active = this.active ?: true
)
