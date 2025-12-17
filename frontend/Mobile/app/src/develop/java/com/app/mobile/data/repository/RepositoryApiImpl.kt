package com.app.mobile.data.repository

import android.content.Context
import com.app.mobile.data.mock.MockDataProvider
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
import com.app.mobile.domain.repository.RepositoryApi
import kotlinx.coroutines.delay

class RepositoryApiImpl(private val context: Context) : RepositoryApi {

    override suspend fun registrationAccount(
        registrationModel: RegistrationModel
    ): RegistrationRequestResult {
        delay(100)
        return RegistrationRequestResult.Success
    }

    override suspend fun confirmationUserRegistration(
        confirmationModel: ConfirmationModel
    ): ConfirmationRequestResult {
        delay(100)
        return ConfirmationRequestResult.Success
    }

    override suspend fun confirmationUserResetPassword(
        confirmationModel: ConfirmationModel
    ): ConfirmationRequestResult {
        delay(100)
        return ConfirmationRequestResult.Success
    }

    override suspend fun authorizationAccount(
        authorizationModel: AuthorizationModel
    ): AuthorizationRequestResult {
        delay(100)
        val mockUser = MockDataProvider.getUser(context)
        return AuthorizationRequestResult.Success(mockUser.jwtToken)
    }

    override suspend fun logoutAccount(): LogoutRequestResult {
        delay(100)
        return LogoutRequestResult.Success
    }

    override suspend fun deleteAccount(): DeleteRequestResult {
        delay(100)
        return DeleteRequestResult.Success
    }

    override suspend fun calcQueenCalendar(queenRequestModel: QueenRequestModel): QueenCalendarRequestResult {
        delay(100)
        val queenLifecycle = MockDataProvider.getQueenLifecycle(context)
        return QueenCalendarRequestResult.Success(queenLifecycle)
    }
}

