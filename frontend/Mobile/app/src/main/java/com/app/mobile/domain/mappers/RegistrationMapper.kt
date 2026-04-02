package com.app.mobile.domain.mappers

import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.data.api.models.HttpCode
import com.app.mobile.data.api.mappers.toErrorMessage
import com.app.mobile.domain.models.UserDomain
import com.app.mobile.domain.models.registration.RegistrationModel
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
    email = email
)

fun ApiResult<Unit>.toRegistrationUiModel(): RegistrationResultUi {
    return when (this) {
        is ApiResult.Success -> RegistrationResultUi.Success

        is ApiResult.HttpError -> when (code) {
            HttpCode.CONFLICT -> RegistrationResultUi.Error("Пользователь уже существует")
            else -> RegistrationResultUi.Error(toErrorMessage())
        }

        else -> RegistrationResultUi.Error(toErrorMessage())
    }
}