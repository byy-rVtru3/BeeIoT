package com.app.mobile.domain.usecase.account

import com.app.mobile.data.session.manager.SessionManager
import com.app.mobile.domain.repository.UserLocalRepository

class IsTokenExistUseCase(
	private val userLocalRepository: UserLocalRepository,
	private val sessionManager: SessionManager
) {
	suspend operator fun invoke(): Boolean {

		val userId = sessionManager.getCurrentUserId() ?: return false

		return userLocalRepository.getUserToken(userId) != null
	}

}