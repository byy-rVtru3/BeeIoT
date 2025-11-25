package com.app.mobile.domain.models.logout

sealed interface LogoutRequestResult {
    data object Success : LogoutRequestResult
    data object UnauthorizedError : LogoutRequestResult
    data object BadRequestError : LogoutRequestResult
    data object ServerError : LogoutRequestResult
    data object TimeoutError : LogoutRequestResult
    data object UnknownError : LogoutRequestResult
}