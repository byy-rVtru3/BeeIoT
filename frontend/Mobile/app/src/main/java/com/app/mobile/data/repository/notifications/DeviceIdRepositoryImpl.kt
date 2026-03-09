package com.app.mobile.data.repository.notifications

import com.app.mobile.data.datastore.DeviceIdDataSource
import com.app.mobile.domain.repository.notifications.DeviceIdRepository

class DeviceIdRepositoryImpl(
	private val dataSource: DeviceIdDataSource,
) : DeviceIdRepository {

	override suspend fun get(): String =
		dataSource.getDeviceId()
}