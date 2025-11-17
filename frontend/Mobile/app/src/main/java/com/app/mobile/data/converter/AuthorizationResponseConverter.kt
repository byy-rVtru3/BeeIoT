package com.app.mobile.data.converter

import com.app.mobile.data.api.models.ResponseApiModel
import com.app.mobile.domain.models.authorization.AuthorizationRequestResult
import retrofit2.Response

class AuthorizationResponseConverter {
    fun convert(response: Response<ResponseApiModel>): AuthorizationRequestResult {
        val token = response.body()?.data?.token
        return if (response.isSuccessful && token != null) {
            AuthorizationRequestResult.Success(token)
        } else {
            handleError(response)
        }
    }

    private fun handleError(response: Response<ResponseApiModel>): AuthorizationRequestResult {
        return when (response.code()) {
            400 -> AuthorizationRequestResult.BadRequestError
            404 -> AuthorizationRequestResult.UserNotFoundError
            500 -> AuthorizationRequestResult.ServerError
            504 -> AuthorizationRequestResult.TimeoutError
            else -> AuthorizationRequestResult.UnknownError
        }
    }

}