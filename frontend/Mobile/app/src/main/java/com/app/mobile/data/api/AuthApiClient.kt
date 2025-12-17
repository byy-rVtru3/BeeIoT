package com.app.mobile.data.api

import com.app.mobile.data.api.models.ResponseApiModel
import com.app.mobile.data.api.models.queen.CalcQueenRequest
import com.app.mobile.data.api.models.queen.QueenCalendarResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST

interface AuthApiClient {

    @DELETE("auth/logout")
    suspend fun logoutAccount(): Response<ResponseApiModel>

    @DELETE("auth/delete/user")
    suspend fun deleteAccount(): Response<ResponseApiModel>

    @POST("calcQueen/calc")
    suspend fun calcQueen(@Body calcQueenRequest: CalcQueenRequest): Response<QueenCalendarResponse>

}