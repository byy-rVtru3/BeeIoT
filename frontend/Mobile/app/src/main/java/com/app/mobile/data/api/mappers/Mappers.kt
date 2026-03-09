package com.app.mobile.data.api.mappers

import com.app.mobile.data.api.models.AuthRequestApiModel
import com.app.mobile.data.api.models.ConfirmationRequestApiModel
import com.app.mobile.data.api.models.PushTokenCreationModel
import com.app.mobile.domain.models.authorization.AuthorizationModel
import com.app.mobile.domain.models.confirmation.ConfirmationModel
import com.app.mobile.domain.models.notifications.PushTokenCreation
import com.app.mobile.domain.models.registration.RegistrationModel

fun RegistrationModel.toApiModel() =
    AuthRequestApiModel(
        email = this.email,
        password = this.password

    )

fun ConfirmationModel.toApiModel() =
    ConfirmationRequestApiModel(
        email = this.email,
        code = this.code
    )

fun AuthorizationModel.toApiModel() =
    AuthRequestApiModel(
        email = this.email,
        password = this.password
    )

fun PushTokenCreation.toApiModel() =
    PushTokenCreationModel(
        deviceId = deviceId,
        token = token
    )