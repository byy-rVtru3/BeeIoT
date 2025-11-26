package com.app.mobile.data.api

import com.app.mobile.data.api.models.ResponseApiModel
import retrofit2.Response
import retrofit2.http.DELETE

interface AuthApiClient {

    @DELETE("auth/logout")
    suspend fun logoutAccount(): Response<ResponseApiModel>

    @DELETE("auth/delete/user")
    suspend fun deleteAccount(): Response<ResponseApiModel>
}