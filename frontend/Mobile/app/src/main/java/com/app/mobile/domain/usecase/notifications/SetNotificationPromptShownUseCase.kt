package com.app.mobile.domain.usecase.notifications

import com.app.mobile.domain.repository.notifications.PermissionRepository

class SetNotificationPromptShownUseCase(
	private val permissionRepository: PermissionRepository
) : suspend (Boolean) -> Unit by permissionRepository::savePermissionAsked