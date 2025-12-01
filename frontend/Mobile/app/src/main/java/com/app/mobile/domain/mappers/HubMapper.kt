package com.app.mobile.domain.mappers

import com.app.mobile.data.database.entity.HubEntity
import com.app.mobile.domain.models.hives.HubDomain
import com.app.mobile.presentation.models.hive.HubUiState

fun HubEntity.toDomain() = HubDomain(
    id = this.id,
    hiveId = this.hiveId,
    name = this.name,
    ipAddress = this.ipAddress,
    port = this.port
)

fun HubDomain?.toUiState(): HubUiState {
    return this?.let { hub ->
        HubUiState.Present(
            name = hub.name,
            ipAddress = hub.ipAddress,
            port = hub.port
        )
    } ?: HubUiState.Absent
}