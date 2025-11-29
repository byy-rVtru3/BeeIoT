package com.app.mobile.domain.mappers

import com.app.mobile.data.database.entity.HubEntity
import com.app.mobile.domain.models.hives.Hub

fun HubEntity.toDomain() = Hub(
    id = this.id,
    hiveId = this.hiveId,
    name = this.name,
    ipAddress = this.ipAddress,
    port = this.port
)