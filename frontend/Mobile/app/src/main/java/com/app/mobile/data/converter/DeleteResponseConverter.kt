package com.app.mobile.data.converter

import com.app.mobile.data.api.models.ResponseApiModel
import com.app.mobile.domain.models.delete.DeleteAccountResult
import retrofit2.Response

class DeleteResponseConverter {
    fun convert(response: Response<ResponseApiModel>): DeleteAccountResult {
        return if (response.isSuccessful) {
            DeleteAccountResult.Success
        } else {
            handleError(response)
        }
    }

    private fun handleError(response: Response<ResponseApiModel>): DeleteAccountResult {
        return when (response.code()) {
            400 -> DeleteAccountResult.BadRequestError
            401 -> DeleteAccountResult.UnauthorizedError
            500 -> DeleteAccountResult.ServerError
            504 -> DeleteAccountResult.TimeoutError
            else -> DeleteAccountResult.UnknownError
        }
    }
}