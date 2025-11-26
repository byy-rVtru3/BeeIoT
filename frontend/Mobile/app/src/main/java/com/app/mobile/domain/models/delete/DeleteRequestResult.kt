package com.app.mobile.domain.models.delete

sealed interface DeleteRequestResult {
    data object Success : DeleteRequestResult
    data object UnauthorizedError : DeleteRequestResult
    data object BadRequestError : DeleteRequestResult
    data object ServerError : DeleteRequestResult
    data object TimeoutError : DeleteRequestResult
    data object UnknownError : DeleteRequestResult
}