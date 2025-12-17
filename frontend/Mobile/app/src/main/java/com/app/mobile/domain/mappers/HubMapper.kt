package com.app.mobile.domain.mappers

import com.app.mobile.data.database.entity.HubEntity
import com.app.mobile.domain.models.hives.HubDomain
import com.app.mobile.presentation.models.hive.HubPreviewModel
import com.app.mobile.presentation.models.hive.HubUi

fun HubEntity.toDomain() = HubDomain(
    id = this.id,
    hiveId = this.hiveId,
    name = this.name,
    ipAddress = this.ipAddress,
    port = this.port
)

fun HubDomain?.toUiModel(): HubUi {
    return this?.let { hub ->
        HubUi.Present(
            id = hub.id,
            name = hub.name,
            ipAddress = hub.ipAddress,
            port = hub.port
        )
    } ?: HubUi.Absent
}

fun HubDomain.toPreviewModel() = HubPreviewModel(
    id = this.id,
    name = this.name
)

fun HubDomain.toEntity() = HubEntity(
    id = this.id,
    hiveId = this.hiveId,
    name = this.name,
    ipAddress = this.ipAddress,
    port = this.port
)