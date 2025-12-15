package com.app.mobile.domain.mappers

import com.app.mobile.data.database.entity.HiveEntity
import com.app.mobile.data.database.entity.HiveWithDetails
import com.app.mobile.data.database.mappers.toDomain
import com.app.mobile.domain.models.hives.HiveDomain
import com.app.mobile.domain.models.hives.HiveDomainPreview
import com.app.mobile.domain.models.hives.HiveEditorDomain
import com.app.mobile.domain.models.hives.HubDomain
import com.app.mobile.domain.models.hives.queen.QueenDomain
import com.app.mobile.presentation.mappers.toPreviewModel
import com.app.mobile.presentation.mappers.toUiModel
import com.app.mobile.presentation.models.hive.HiveEditorModel
import com.app.mobile.presentation.models.hive.HivePreview
import com.app.mobile.presentation.models.hive.HiveUi


fun HiveWithDetails.toDomain() = HiveDomain(
    id = hive.id,
    name = hive.name,
    connectedHub = connectedHub?.toDomain(),
    notifications = notifications?.map { it.toDomain() } ?: emptyList(),
    works = works?.map { it.toDomain() } ?: emptyList(),
    queen = queen?.toDomain()
)

fun HiveDomainPreview.toHivePreview() = HivePreview(
    id = this.id,
    name = this.name
)

fun HiveEntity.toDomain() = HiveDomainPreview(
    id = this.id,
    name = this.name
)

fun HiveDomain.toUiModel() = HiveUi(
    id = this.id,
    name = this.name,
    connectedHub = this.connectedHub.toUiModel(),
    notifications = this.notifications.map { it.toUiModel() },
    queen = this.queen.toUiModel(),
    works = this.works.map { it.toUiModel() }
)

fun HiveDomain.toEditor(queens: List<QueenDomain>, hubs: List<HubDomain>) = HiveEditorModel(
    id = this.id,
    name = this.name,
    connectedHubId = this.connectedHub?.id,
    hubs = hubs.map { it.toPreviewModel() },
    connectedQueenId = this.queen?.id,
    queens = queens.map { it.toPreviewModel() }
)

fun HiveEditorDomain.toPresentation(queens: List<QueenDomain>, hubs: List<HubDomain>) =
    HiveEditorModel(
        id = this.id,
        name = this.name,
        connectedHubId = this.connectedHubId,
        hubs = hubs.map { it.toPreviewModel() },
        connectedQueenId = this.connectedQueenId,
        queens = queens.map { it.toPreviewModel() }
    )

fun HiveEditorDomain.toEntity() = HiveEntity(
    id = this.id,
    name = this.name
)

fun HiveEditorModel.toDomain() = HiveEditorDomain(
    id = this.id,
    name = this.name,
    connectedHubId = this.connectedHubId,
    connectedQueenId = this.connectedQueenId
)