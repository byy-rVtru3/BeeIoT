package com.app.mobile.domain.mappers

import com.app.mobile.domain.models.hives.HubDomain
import com.app.mobile.presentation.models.hub.HubDetailUi
import com.app.mobile.presentation.models.hub.HubEditorModel
import com.app.mobile.presentation.models.hive.HubPreviewModel
import com.app.mobile.presentation.models.hive.HubUi

fun HubDomain?.toUiModel(): HubUi {
    return this?.let { hub ->
        HubUi.Present(
            id = hub.id,
            name = hub.name
        )
    } ?: HubUi.Absent
}

fun HubDomain.toPreviewModel() = HubPreviewModel(
    id = this.id,
    name = this.name
)

fun HubDomain.toDetailUi() = HubDetailUi(
    id = this.id,
    name = this.name,
    ipAddress = this.ipAddress,
    port = this.port
)

fun HubDomain.toEditorModel() = HubEditorModel(
    id = this.id,
    hiveId = this.hiveId,
    name = this.name,
    ipAddress = this.ipAddress,
    port = this.port.toString()
)

fun HubEditorModel.toDomain() = HubDomain(
    id = this.id,
    hiveId = this.hiveId,
    name = this.name,
    ipAddress = this.ipAddress,
    port = this.port.toIntOrNull() ?: 0
)

fun HubDomain.toEntity() = HubEntity(
    id = this.id,
    hiveId = this.hiveId,
    name = this.name,
    ipAddress = this.ipAddress,
    port = this.port
)
