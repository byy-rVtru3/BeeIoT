package com.app.mobile.data.repository

import com.app.mobile.data.api.AuthApiClient
import com.app.mobile.data.api.PublicApiClient
import com.app.mobile.data.api.mappers.toApiModel
import com.app.mobile.data.api.mappers.toDomain
import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.data.api.models.AuthRequestApiModel
import com.app.mobile.data.api.models.account.ChangeNameRequest
import com.app.mobile.data.api.safeApiCall
import com.app.mobile.domain.mappers.toDomain
import com.app.mobile.domain.models.UserDomain
import com.app.mobile.domain.models.authorization.AuthorizationModel
import com.app.mobile.domain.models.confirmation.ConfirmationModel
import com.app.mobile.domain.models.hives.queen.QueenLifecycle
import com.app.mobile.domain.models.hives.queen.QueenRequestModel
import com.app.mobile.domain.models.notifications.PushTokenCreation
import com.app.mobile.domain.models.registration.RegistrationModel
import com.app.mobile.domain.repository.RepositoryApi

class RepositoryApiImpl(
    private val publicApiClient: PublicApiClient,
    private val authApiClient: AuthApiClient,
) : RepositoryApi {

    override suspend fun registrationAccount(registrationModel: RegistrationModel): ApiResult<Unit> =
        safeApiCall { publicApiClient.registrationAccount(registrationModel.toApiModel()) }

    override suspend fun confirmationUserRegistration(confirmationModel: ConfirmationModel): ApiResult<Unit> =
        safeApiCall { publicApiClient.confirmRegistrationAccount(confirmationModel.toApiModel()) }

    override suspend fun confirmationUserResetPassword(confirmationModel: ConfirmationModel): ApiResult<Unit> =
        safeApiCall { publicApiClient.confirmResetPassword(confirmationModel.toApiModel()) }

    override suspend fun authorizationAccount(authorizationModel: AuthorizationModel): ApiResult<String> =
        safeApiCall(
            apiCall = { publicApiClient.authorizationAccount(authorizationModel.toApiModel()) },
            onSuccess = { it.data?.token ?: throw IllegalStateException("Token is null") }
        )

    override suspend fun logoutAccount(): ApiResult<Unit> =
        safeApiCall { authApiClient.logoutAccount() }

    override suspend fun deleteAccount(): ApiResult<Unit> =
        safeApiCall { authApiClient.deleteAccount() }

    override suspend fun calcQueenCalendar(queenRequestModel: QueenRequestModel): ApiResult<QueenLifecycle> =
        safeApiCall(
            apiCall = { authApiClient.calcQueen(queenRequestModel.toApiModel()) },
            onSuccess = { it.data.toDomain() }
        )

    override suspend fun registerPushToken(pushTokenCreation: PushTokenCreation): ApiResult<Unit> =
        safeApiCall { authApiClient.registerPushToken(pushTokenCreation.toApiModel()) }

    override suspend fun getAccountInfo(): ApiResult<UserDomain> =
        safeApiCall(
            apiCall = { authApiClient.getMe() },
            onSuccess = { it.data.toDomain() }
        )

    override suspend fun updateName(name: String): ApiResult<Unit> =
        safeApiCall { authApiClient.updateName(ChangeNameRequest(name)) }

    override suspend fun initiatePasswordChange(email: String, newPassword: String): ApiResult<Unit> =
        safeApiCall { publicApiClient.changePassword(AuthRequestApiModel(email, newPassword)) }
}