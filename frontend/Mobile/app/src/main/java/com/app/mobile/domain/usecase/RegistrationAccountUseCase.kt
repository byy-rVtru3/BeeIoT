package com.app.mobile.domain.usecase

import com.app.mobile.domain.mappers.toApiModel
import com.app.mobile.domain.models.registration.RegistrationRequestModel
import com.app.mobile.domain.models.registration.RegistrationResponseModel
import com.app.mobile.domain.repository.Repository

class RegistrationAccountUseCase(
    private val repository: Repository
) {

    suspend operator fun invoke(registrationRequestModel: RegistrationRequestModel): RegistrationResponseModel {
        return repository.registrationAccount(registrationRequestModel.toApiModel())
    }

}