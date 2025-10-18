package com.app.mobile.domain.models.registration

sealed class RegistrationRequestResult {
    object Success : RegistrationRequestResult()
    object UserAlreadyExists : RegistrationRequestResult()
    object ServerError : RegistrationRequestResult()
    object UnknownError : RegistrationRequestResult()
}
