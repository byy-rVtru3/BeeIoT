package com.app.mobile.domain.repository.notifications

interface DeviceIdRepository {

	suspend fun get(): String
}