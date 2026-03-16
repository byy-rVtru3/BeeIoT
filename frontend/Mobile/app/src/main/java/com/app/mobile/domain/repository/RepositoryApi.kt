package com.app.mobile.domain.repository

import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.domain.models.authorization.AuthorizationModel
import com.app.mobile.domain.models.confirmation.ConfirmationModel
import com.app.mobile.domain.models.hives.queen.QueenLifecycle
import com.app.mobile.domain.models.hives.queen.QueenRequestModel
import com.app.mobile.domain.models.notifications.PushTokenCreation
import com.app.mobile.domain.models.registration.RegistrationModel

interface RepositoryApi {
    suspend fun registrationAccount(registrationModel: RegistrationModel): ApiResult<Unit>

    suspend fun confirmationUserRegistration(confirmationModel: ConfirmationModel): ApiResult<Unit>

    suspend fun confirmationUserResetPassword(confirmationModel: ConfirmationModel): ApiResult<Unit>

    suspend fun authorizationAccount(authorizationModel: AuthorizationModel): ApiResult<String>

    suspend fun logoutAccount(): ApiResult<Unit>

    suspend fun deleteAccount(): ApiResult<Unit>

    suspend fun calcQueenCalendar(queenRequestModel: QueenRequestModel): ApiResult<QueenLifecycle>

    suspend fun registerPushToken(pushTokenCreation: PushTokenCreation): ApiResult<Unit>
}