package com.app.mobile.data.repository

import android.content.Context
import com.app.mobile.data.mock.MockDataProvider
import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.domain.models.authorization.AuthorizationModel
import com.app.mobile.domain.models.confirmation.ConfirmationModel
import com.app.mobile.domain.models.hives.queen.QueenLifecycle
import com.app.mobile.domain.models.hives.queen.QueenRequestModel
import com.app.mobile.domain.models.notifications.PushTokenCreation
import com.app.mobile.domain.models.registration.RegistrationModel
import com.app.mobile.domain.repository.RepositoryApi
import kotlinx.coroutines.delay

class RepositoryApiImpl(private val context: Context) : RepositoryApi {

    override suspend fun registrationAccount(
        registrationModel: RegistrationModel
    ): ApiResult<Unit> {
        delay(100)
        return ApiResult.Success(Unit)
    }

    override suspend fun confirmationUserRegistration(
        confirmationModel: ConfirmationModel
    ): ApiResult<Unit> {
        delay(100)
        return ApiResult.Success(Unit)
    }

    override suspend fun confirmationUserResetPassword(
        confirmationModel: ConfirmationModel
    ): ApiResult<Unit> {
        delay(100)
        return ApiResult.Success(Unit)
    }

    override suspend fun authorizationAccount(
        authorizationModel: AuthorizationModel
    ): ApiResult<String> {
        delay(100)
        val mockUser = MockDataProvider.getUser(context)
        return ApiResult.Success(mockUser.jwtToken)
    }

    override suspend fun logoutAccount(): ApiResult<Unit> {
        delay(100)
        return ApiResult.Success(Unit)
    }

    override suspend fun deleteAccount(): ApiResult<Unit> {
        delay(100)
        return ApiResult.Success(Unit)
    }

    override suspend fun calcQueenCalendar(queenRequestModel: QueenRequestModel): ApiResult<QueenLifecycle> {
        delay(100)
        val queenLifecycle = MockDataProvider.getQueenLifecycle(context)
        return ApiResult.Success(queenLifecycle)
    }

    override suspend fun registerPushToken(pushTokenCreation: PushTokenCreation): ApiResult<Unit> {
        delay(100)
        return ApiResult.Success(Unit)
    }
}