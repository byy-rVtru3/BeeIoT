package com.app.mobile.domain.mappers

import com.app.mobile.domain.models.hives.HiveDomain
import com.app.mobile.domain.models.hives.HiveDomainPreview
import com.app.mobile.domain.models.hives.HiveEditorDomain
import com.app.mobile.domain.models.hives.HubDomain
import com.app.mobile.domain.models.hives.queen.QueenDomainPreview
import com.app.mobile.presentation.models.hive.HiveEditorModel
import com.app.mobile.presentation.models.hive.HivePreview

fun HiveDomainPreview.toHivePreview() = HivePreview(
	name = this.name
)

fun HiveDomain.toEditor(queens: List<QueenDomainPreview>, hubs: List<HubDomain>) = HiveEditorModel(
	name = this.name,
	connectedHubId = this.hub?.id,
	hubs = hubs.map { it.toPreviewModel() },
	connectedQueenName = this.queen?.name,
	queens = queens.map { it.toPreviewListModel() }
)

fun HiveEditorDomain.toPresentation(queens: List<QueenDomainPreview>, hubs: List<HubDomain>) =
	HiveEditorModel(
		name = this.name,
		connectedHubId = this.connectedHubId,
		hubs = hubs.map { it.toPreviewModel() },
		connectedQueenName = this.connectedQueenName,
		queens = queens.map { it.toPreviewListModel() }
	)

fun HiveEditorModel.toDomain() = HiveEditorDomain(
	name = this.name,
	connectedHubId = this.connectedHubId,
	connectedQueenName = this.connectedQueenName
)
