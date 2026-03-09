package com.app.mobile.data.converter

import com.app.mobile.data.api.models.ResponseApiModel
import com.app.mobile.domain.models.notifications.PushTokenRequestResult
import retrofit2.Response

class PushTokenConverter {

	fun convert(response: Response<ResponseApiModel>): PushTokenRequestResult {
		return if (response.isSuccessful) {
			PushTokenRequestResult.Success
		} else {
			handleError(response)
		}
	}

	private fun handleError(response: Response<ResponseApiModel>): PushTokenRequestResult {
		return when (response.code()) {
			400  -> PushTokenRequestResult.BadRequestError
			500  -> PushTokenRequestResult.ServerError
			504  -> PushTokenRequestResult.TimeoutError
			else -> PushTokenRequestResult.UnknownError
		}
	}

}