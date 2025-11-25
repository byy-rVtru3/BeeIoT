package com.app.mobile.domain.models.delete

sealed interface DeleteAccountResult {
    data object Success : DeleteAccountResult
    data object UnauthorizedError : DeleteAccountResult
    data object BadRequestError : DeleteAccountResult
    data object ServerError : DeleteAccountResult
    data object TimeoutError : DeleteAccountResult
    data object UnknownError : DeleteAccountResult
}