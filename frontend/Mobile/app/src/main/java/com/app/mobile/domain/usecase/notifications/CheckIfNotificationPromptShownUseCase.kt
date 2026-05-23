package com.app.mobile.domain.usecase.notifications

import com.app.mobile.domain.repository.notifications.PermissionRepository

class CheckIfNotificationPromptShownUseCase(
	private val permissionRepository: PermissionRepository
) : suspend () -> Boolean by permissionRepository::hasAskedForPermission