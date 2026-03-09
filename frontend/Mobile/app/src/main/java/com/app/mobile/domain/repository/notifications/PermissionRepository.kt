package com.app.mobile.domain.repository.notifications

interface PermissionRepository {

	suspend fun hasAskedForPermission(): Boolean

	suspend fun savePermissionAsked(hasAsked: Boolean)
}