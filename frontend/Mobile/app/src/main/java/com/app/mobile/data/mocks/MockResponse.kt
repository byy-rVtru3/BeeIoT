package com.app.mobile.data.mocks

import com.app.mobile.data.api.models.registration.RegistrationResponseApiModel

val mockResponseToRegistrationSuccess = RegistrationResponseApiModel(
    code = 201,
    message = "User registered successfully"
)

val mockResponseToRegistrationError = RegistrationResponseApiModel(
    code = 400,
    message = "Email already exists"
)

fun mockResponseToConfirmationUserSuccess(code: String): Boolean {
    return code == "123456"
}