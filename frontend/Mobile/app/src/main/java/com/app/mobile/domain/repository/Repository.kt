package com.app.mobile.domain.repository

import com.app.mobile.data.api.models.AuthRequestApiModel
import com.app.mobile.domain.models.confirmation.ConfirmationModel
import com.app.mobile.domain.models.confirmation.ConfirmationRequestResult
import com.app.mobile.domain.models.registration.RegistrationModel
import com.app.mobile.domain.models.registration.RegistrationRequestResult

interface Repository {
    suspend fun registrationAccount(
        registrationModel: RegistrationModel): RegistrationRequestResult

    suspend fun confirmationUser(confirmationModel: ConfirmationModel): ConfirmationRequestResult

    suspend fun resendConfirmationCode(email: String, type: String)
}