package com.app.mobile.data.repository

import android.util.Log
import com.app.mobile.data.api.BeeApiClient
import com.app.mobile.data.api.mappers.toApiModel
import com.app.mobile.data.converter.ConfirmationResponseConverter
import com.app.mobile.data.converter.RegistrationResponseConverter
import com.app.mobile.domain.models.confirmation.ConfirmationModel
import com.app.mobile.domain.models.confirmation.ConfirmationRequestResult
import com.app.mobile.domain.models.registration.RegistrationModel
import com.app.mobile.domain.models.registration.RegistrationRequestResult
import com.app.mobile.domain.repository.Repository

class RepositoryImpl(
    private val beeApiClient: BeeApiClient,
    private val registrationResponseConverter: RegistrationResponseConverter,
    private val confirmationResponseConverter: ConfirmationResponseConverter
) : Repository {
    override suspend fun registrationAccount(
        registrationModel: RegistrationModel
    ): RegistrationRequestResult {
        return try {
            val response = beeApiClient.registrationAccount(registrationModel.toApiModel())
            registrationResponseConverter.convert(response)

        } catch (e: Exception) {
            // так делают только чмошники но мне похуй
            Log.e("RepositoryImpl", "Error during registrationAccount", e)
            RegistrationRequestResult.UnknownError
        }

    }

    override suspend fun confirmationUserRegistration(confirmationModel: ConfirmationModel): ConfirmationRequestResult {
        return try {
            val response = beeApiClient.confirmRegistrationAccount(confirmationModel.toApiModel())
            confirmationResponseConverter.convert(response)

        } catch (e: Exception) {
            // так делают только чмошники но мне похуй
            Log.e("RepositoryImpl", "Error during confirmationUser", e)
            ConfirmationRequestResult.UnknownError
        }
    }

    override suspend fun confirmationUserResetPassword(confirmationModel: ConfirmationModel): ConfirmationRequestResult {
        return try {
            val response = beeApiClient.confirmResetPassword(confirmationModel.toApiModel())
            confirmationResponseConverter.convert(response)

        } catch (e: Exception) {
            // так делают только чмошники но мне похуй
            Log.e("RepositoryImpl", "Error during confirmationUserResetPassword", e)
            ConfirmationRequestResult.UnknownError
        }
    }

}