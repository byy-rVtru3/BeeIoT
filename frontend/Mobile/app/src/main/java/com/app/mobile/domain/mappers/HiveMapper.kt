package com.app.mobile.domain.mappers

import com.app.mobile.data.database.entity.HiveEntity
import com.app.mobile.data.database.entity.HiveWithDetails
import com.app.mobile.domain.models.hives.HiveDomain
import com.app.mobile.domain.models.hives.HiveDomainPreview
import com.app.mobile.domain.models.hives.HubDomain
import com.app.mobile.presentation.models.hive.HivePreview
import com.app.mobile.presentation.models.hive.HiveUi
import com.app.mobile.presentation.models.hive.HubUiState


fun HiveWithDetails.toDomain() = HiveDomain(
    id = hive.id,
    name = hive.name,
    connectedHub = connectedHub.toDomain(),
    notifications = notifications.map { it.toDomain() },
    works = works.map { it.toDomain() },
    queen = queen.toDomain()
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
    connectedHub = this.connectedHub.toUiState(),
    notifications = this.notifications.orEmpty().map { it.toUiModel() },
    queen = this.queen.toUiState(),
    works = this.works.orEmpty().map { it.toUiModel() }
)