package com.app.mobile.domain.models.notifications

interface PushTokenRequestResult {
	data object Success : PushTokenRequestResult
	data object ServerError : PushTokenRequestResult
	data object UnknownError : PushTokenRequestResult
	data object BadRequestError : PushTokenRequestResult
	data object TimeoutError : PushTokenRequestResult
}