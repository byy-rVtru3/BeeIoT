package com.app.mobile.domain.usecase.hives.hub

import com.app.mobile.domain.repository.datasource.HubDataSource

class SaveHubUseCase(private val hubDataSource: HubDataSource) {

	suspend operator fun invoke(name: String, id: String, isNew: Boolean) =
		if (isNew) {
			hubDataSource.createHub(name = name, id = id)
		} else {
			hubDataSource.updateHub(name = name, id = id)
		}
}
