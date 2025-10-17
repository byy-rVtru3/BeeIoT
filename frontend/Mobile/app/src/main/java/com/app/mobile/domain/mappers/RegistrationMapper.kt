package com.app.mobile.domain.mappers

import com.app.mobile.data.api.models.registration.RegistrationRequestApiModel
import com.app.mobile.domain.models.registration.RegistrationModel
import com.app.mobile.domain.models.registration.RegistrationRequestResult
import com.app.mobile.presentation.models.RegistrationModelUi
import com.app.mobile.presentation.models.RegistrationResultUi

fun RegistrationModel.toApiModel() =
    RegistrationRequestApiModel(
        email = this.email,
        password = this.password
    )

fun RegistrationModel.toUiModel(repeatPassword: String = "") = RegistrationModelUi(
    email = email,
    name = name,
    password = password,
    repeatPassword = repeatPassword
)

fun RegistrationRequestResult.toUiModel(): RegistrationResultUi {
    return when (this) {
        is RegistrationRequestResult.Success -> RegistrationResultUi.Success
        is RegistrationRequestResult.ServerError -> RegistrationResultUi.Error(
            "Ошибка сервера")

        is RegistrationRequestResult.UserAlreadyExists -> RegistrationResultUi.Error(
            "Пользователь уже существует")

        is RegistrationRequestResult.UnknownError -> RegistrationResultUi.Error(
            "Неизвестная ошибка")
    }
}