package com.app.mobile.data.api

import com.app.mobile.data.api.models.registration.RegistrationRequestApiModel
import com.app.mobile.data.api.models.registration.RegistrationResponseApiModel
import retrofit2.http.Body
import retrofit2.http.POST

fun interface BeeApiClient {
    @POST("register")
    suspend fun registrationAccount(@Body request: RegistrationRequestApiModel): RegistrationResponseApiModel
}