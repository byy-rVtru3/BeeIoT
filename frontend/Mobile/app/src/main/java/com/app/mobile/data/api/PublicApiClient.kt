package com.app.mobile.data.api

import com.app.mobile.data.api.models.AuthRequestApiModel
import com.app.mobile.data.api.models.ConfirmationRequestApiModel
import com.app.mobile.data.api.models.ResponseApiModel
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface PublicApiClient {
    @POST("auth/registration")
    suspend fun registrationAccount(@Body request: AuthRequestApiModel):
        Response<ResponseApiModel>

    @POST("auth/confirm/registration")
    suspend fun confirmRegistrationAccount(@Body request: ConfirmationRequestApiModel):
        Response<ResponseApiModel>

    @POST("auth/confirm/password")
    suspend fun confirmResetPassword(@Body request: ConfirmationRequestApiModel):
        Response<ResponseApiModel>

    @POST("auth/login")
    suspend fun authorizationAccount(@Body request: AuthRequestApiModel):
        Response<ResponseApiModel>
}