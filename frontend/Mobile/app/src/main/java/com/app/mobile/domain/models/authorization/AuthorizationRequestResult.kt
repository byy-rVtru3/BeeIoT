package com.app.mobile.domain.models.authorization

sealed interface AuthorizationRequestResult {
    data class Success(val token: String) : AuthorizationRequestResult
    data object BadRequestError : AuthorizationRequestResult
    data object UserNotFoundError : AuthorizationRequestResult
    data object ServerError : AuthorizationRequestResult
    data object TimeoutError : AuthorizationRequestResult
    data object UnknownError : AuthorizationRequestResult
}