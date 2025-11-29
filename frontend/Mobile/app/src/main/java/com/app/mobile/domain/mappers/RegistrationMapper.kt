package com.app.mobile.domain.mappers

import com.app.mobile.domain.models.UserDomain
import com.app.mobile.domain.models.registration.RegistrationModel
import com.app.mobile.domain.models.registration.RegistrationRequestResult
import com.app.mobile.presentation.models.account.RegistrationModelUi
import com.app.mobile.presentation.models.account.RegistrationResultUi

fun RegistrationModel.toUiModel(repeatPassword: String = "") =
    RegistrationModelUi(
        name = name,
        email = email,
        password = password,
        repeatPassword = repeatPassword
    )

fun RegistrationModel.toUserDomain() = UserDomain(
    name = name,
    email = email,
    password = password,
    jwtToken = null
)

fun RegistrationRequestResult.toUiModel(): RegistrationResultUi {
    return when (this) {
        is RegistrationRequestResult.Success -> RegistrationResultUi.Success

        is RegistrationRequestResult.ServerError -> RegistrationResultUi.Error(
            "Ошибка сервера"
        )

        is RegistrationRequestResult.UserAlreadyExistsError -> RegistrationResultUi.Error(
            "Пользователь уже существует"
        )

        is RegistrationRequestResult.UnknownError -> RegistrationResultUi.Error(
            "Неизвестная ошибка"
        )

        is RegistrationRequestResult.TimeoutError -> RegistrationResultUi.Error(
            "Превышено время ожидания"
        )

        is RegistrationRequestResult.BadRequestError -> RegistrationResultUi.Error(
            "Некорректный запрос"
        )
    }
}