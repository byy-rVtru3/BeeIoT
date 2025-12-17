package com.app.mobile.domain.models.confirmation

sealed interface ConfirmationRequestResult {
    data object Success : ConfirmationRequestResult
    data object BadRequestError : ConfirmationRequestResult
    data object UnauthorizedError : ConfirmationRequestResult
    data object ServerError : ConfirmationRequestResult
    data object TimeoutError : ConfirmationRequestResult
    data object NotFoundError : ConfirmationRequestResult
    data object UnknownError : ConfirmationRequestResult
}