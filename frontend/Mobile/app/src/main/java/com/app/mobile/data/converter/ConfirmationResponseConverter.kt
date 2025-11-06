package com.app.mobile.data.converter

import com.app.mobile.data.api.models.ResponseApiModel
import com.app.mobile.domain.models.confirmation.ConfirmationRequestResult
import retrofit2.Response

class ConfirmationResponseConverter {
    fun convert(response: Response<ResponseApiModel>): ConfirmationRequestResult {
        return if (response.isSuccessful) {
            ConfirmationRequestResult.Success
        } else {
            handleError(response)
        }
    }

    private fun handleError(response: Response<ResponseApiModel>): ConfirmationRequestResult {
        return when (response.code()) {
            400 -> ConfirmationRequestResult.BadRequestError
            401 -> ConfirmationRequestResult.UnauthorizedError
            404 -> ConfirmationRequestResult.NotFoundError
            500 -> ConfirmationRequestResult.ServerError
            504 -> ConfirmationRequestResult.TimeoutError
            else -> ConfirmationRequestResult.UnknownError
        }
    }
}