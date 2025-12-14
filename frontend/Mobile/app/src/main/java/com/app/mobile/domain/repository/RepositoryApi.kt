package com.app.mobile.domain.repository

import com.app.mobile.domain.models.authorization.AuthorizationModel
import com.app.mobile.domain.models.authorization.AuthorizationRequestResult
import com.app.mobile.domain.models.confirmation.ConfirmationModel
import com.app.mobile.domain.models.confirmation.ConfirmationRequestResult
import com.app.mobile.domain.models.delete.DeleteRequestResult
import com.app.mobile.domain.models.hives.queen.QueenCalendarRequestResult
import com.app.mobile.domain.models.hives.queen.QueenRequestModel
import com.app.mobile.domain.models.logout.LogoutRequestResult
import com.app.mobile.domain.models.registration.RegistrationModel
import com.app.mobile.domain.models.registration.RegistrationRequestResult

interface RepositoryApi {
    suspend fun registrationAccount(registrationModel: RegistrationModel): RegistrationRequestResult

    suspend fun confirmationUserRegistration(confirmationModel: ConfirmationModel): ConfirmationRequestResult

    suspend fun confirmationUserResetPassword(confirmationModel: ConfirmationModel): ConfirmationRequestResult

    suspend fun authorizationAccount(authorizationModel: AuthorizationModel): AuthorizationRequestResult

    suspend fun logoutAccount(): LogoutRequestResult

    suspend fun deleteAccount(): DeleteRequestResult

    suspend fun calcQueenCalendar(queenRequestModel: QueenRequestModel): QueenCalendarRequestResult
}