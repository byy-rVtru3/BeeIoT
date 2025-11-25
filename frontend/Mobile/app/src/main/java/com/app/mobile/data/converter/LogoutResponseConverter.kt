package com.app.mobile.data.converter

import com.app.mobile.data.api.models.ResponseApiModel
import com.app.mobile.domain.models.logout.LogoutRequestResult
import retrofit2.Response

class LogoutResponseConverter {
    fun convert(response: Response<ResponseApiModel>): LogoutRequestResult {
        return if (response.isSuccessful) {
            LogoutRequestResult.Success
        } else {
            handleError(response)
        }
    }

    private fun handleError(response: Response<ResponseApiModel>): LogoutRequestResult {
        return when (response.code()) {
            400 -> LogoutRequestResult.BadRequestError
            401 -> LogoutRequestResult.UnauthorizedError
            500 -> LogoutRequestResult.ServerError
            504 -> LogoutRequestResult.TimeoutError
            else -> LogoutRequestResult.UnknownError
        }
    }

}