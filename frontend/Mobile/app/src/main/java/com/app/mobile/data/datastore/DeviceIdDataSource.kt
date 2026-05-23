package com.app.mobile.data.datastore

import com.google.firebase.installations.FirebaseInstallations
import kotlinx.coroutines.tasks.await

interface DeviceIdDataSource {

	suspend fun getDeviceId(): String
}

class DeviceIdDataSourceImpl(
	private val firebaseInstallations: FirebaseInstallations
) : DeviceIdDataSource {

	override suspend fun getDeviceId(): String =
		firebaseInstallations.id.await()
}