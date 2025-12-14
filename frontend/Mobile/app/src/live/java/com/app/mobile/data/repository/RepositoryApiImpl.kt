package com.app.mobile.data.repository

import android.util.Log
import com.app.mobile.data.api.AuthApiClient
import com.app.mobile.data.api.PublicApiClient
import com.app.mobile.data.api.mappers.toApiModel
import com.app.mobile.data.converter.AuthorizationResponseConverter
import com.app.mobile.data.converter.ConfirmationResponseConverter
import com.app.mobile.data.converter.DeleteResponseConverter
import com.app.mobile.data.converter.LogoutResponseConverter
import com.app.mobile.data.converter.RegistrationResponseConverter
import com.app.mobile.domain.models.authorization.AuthorizationModel
import com.app.mobile.domain.models.authorization.AuthorizationRequestResult
import com.app.mobile.domain.models.confirmation.ConfirmationModel
import com.app.mobile.domain.models.confirmation.ConfirmationRequestResult
import com.app.mobile.domain.models.delete.DeleteRequestResult
import com.app.mobile.domain.models.logout.LogoutRequestResult
import com.app.mobile.domain.models.registration.RegistrationModel
import com.app.mobile.domain.models.registration.RegistrationRequestResult
import com.app.mobile.domain.repository.RepositoryApi
import retrofit2.Response

class RepositoryApiImpl(
    private val publicApiClient: PublicApiClient,
    private val authApiClient: AuthApiClient,
    private val registrationResponseConverter: RegistrationResponseConverter,
    private val confirmationResponseConverter: ConfirmationResponseConverter,
    private val authorizationResponseConverter: AuthorizationResponseConverter,
    private val logoutResponseConverter: LogoutResponseConverter,
    private val deleteResponseConverter: DeleteResponseConverter
) : RepositoryApi {

    private suspend fun <T, R> executeRequest(
        apiCall: suspend () -> Response<T>,
        converter: (Response<T>) -> R,
        errorResult: R,
        logMessage: String
    ): R {
        return try {
            val response = apiCall()
            converter(response)
        } catch (e: Exception) {
            Log.e("RepositoryImpl", logMessage, e)
            errorResult
        }
    }

    override suspend fun registrationAccount(
        registrationModel: RegistrationModel
    ): RegistrationRequestResult {
        return executeRequest(
            apiCall = { publicApiClient.registrationAccount(registrationModel.toApiModel()) },
            converter = { registrationResponseConverter.convert(it) },
            errorResult = RegistrationRequestResult.UnknownError,
            logMessage = "Error during registrationAccount"
        )
    }

    override suspend fun confirmationUserRegistration(confirmationModel: ConfirmationModel) =
        executeRequest(
            apiCall = { publicApiClient.confirmRegistrationAccount(confirmationModel.toApiModel()) },
            converter = { confirmationResponseConverter.convert(it) },
            errorResult = ConfirmationRequestResult.UnknownError,
            logMessage = "Error during confirmationUser"
        )

    override suspend fun confirmationUserResetPassword(confirmationModel: ConfirmationModel) =
        executeRequest(
            apiCall = { publicApiClient.confirmResetPassword(confirmationModel.toApiModel()) },
            converter = { confirmationResponseConverter.convert(it) },
            errorResult = ConfirmationRequestResult.UnknownError,
            logMessage = "Error during confirmationUserResetPassword"
        )

    override suspend fun authorizationAccount(authorizationModel: AuthorizationModel) =
        executeRequest(
            apiCall = { publicApiClient.authorizationAccount(authorizationModel.toApiModel()) },
            converter = { authorizationResponseConverter.convert(it) },
            errorResult = AuthorizationRequestResult.UnknownError,
            logMessage = "Error during authorizationAccount"
        )

    override suspend fun logoutAccount() =
        executeRequest(
            apiCall = { authApiClient.logoutAccount() },
            converter = { logoutResponseConverter.convert(it) },
            errorResult = LogoutRequestResult.UnknownError,
            logMessage = "Error during logoutAccount"
        )

    override suspend fun deleteAccount() =
        executeRequest(
            apiCall = { authApiClient.deleteAccount() },
            converter = { deleteResponseConverter.convert(it) },
            errorResult = DeleteRequestResult.UnknownError,
            logMessage = "Error during deleteAccount"
        )
}

