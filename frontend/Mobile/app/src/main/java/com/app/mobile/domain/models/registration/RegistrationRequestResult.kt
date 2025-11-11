package com.app.mobile.domain.models.registration

sealed interface RegistrationRequestResult {
    data object Success : RegistrationRequestResult
    data object UserAlreadyExistsError : RegistrationRequestResult
    data object ServerError : RegistrationRequestResult
    data object UnknownError : RegistrationRequestResult
    data object BadRequestError : RegistrationRequestResult
    data object TimeoutError : RegistrationRequestResult
}
