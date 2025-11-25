package com.app.mobile.data.converter

import com.app.mobile.data.api.models.ResponseApiModel
import com.app.mobile.domain.models.delete.DeleteRequestResult
import retrofit2.Response

class DeleteResponseConverter {
    fun convert(response: Response<ResponseApiModel>): DeleteRequestResult {
        return if (response.isSuccessful) {
            DeleteRequestResult.Success
        } else {
            handleError(response)
        }
    }

    private fun handleError(response: Response<ResponseApiModel>): DeleteRequestResult {
        return when (response.code()) {
            400 -> DeleteRequestResult.BadRequestError
            401 -> DeleteRequestResult.UnauthorizedError
            500 -> DeleteRequestResult.ServerError
            504 -> DeleteRequestResult.TimeoutError
            else -> DeleteRequestResult.UnknownError
        }
    }
}