package com.app.mobile.data.api

import com.app.mobile.data.api.models.PushTokenCreationModel
import com.app.mobile.data.api.models.ResponseApiModel
import com.app.mobile.data.api.models.account.ChangeNameRequest
import com.app.mobile.data.api.models.account.UserMeApiResponse
import com.app.mobile.data.api.models.hive.CreateHiveRequest
import com.app.mobile.data.api.models.hive.DeleteHiveRequest
import com.app.mobile.data.api.models.hive.HiveDetailsResponse
import com.app.mobile.data.api.models.hive.HiveListResponse
import com.app.mobile.data.api.models.hive.LinkToHiveRequest
import com.app.mobile.data.api.models.hive.UpdateHiveRequest
import com.app.mobile.data.api.models.hub.CreateHubRequest
import com.app.mobile.data.api.models.hub.DeleteHubRequest
import com.app.mobile.data.api.models.hub.HubDetailsResponse
import com.app.mobile.data.api.models.hub.HubListResponse
import com.app.mobile.data.api.models.hub.UpdateHubRequest
import com.app.mobile.data.api.models.telemetry.LastSensorReadingResponse
import com.app.mobile.data.api.models.telemetry.SetWeightRequest
import com.app.mobile.data.api.models.telemetry.TelemetryHistoryResponse
import com.app.mobile.data.api.models.queen.CalcQueenRequest
import com.app.mobile.data.api.models.queen.CreateQueenRequest
import com.app.mobile.data.api.models.queen.DeleteQueenRequest
import com.app.mobile.data.api.models.queen.QueenCalendarResponse
import com.app.mobile.data.api.models.queen.QueenDetailsResponse
import com.app.mobile.data.api.models.queen.QueenListResponse
import com.app.mobile.data.api.models.queen.UpdateQueenRequest
import com.app.mobile.data.api.models.task.CreateTaskRequest
import com.app.mobile.data.api.models.task.DeleteTaskRequest
import com.app.mobile.data.api.models.task.TaskCreateResponse
import com.app.mobile.data.api.models.task.TaskListResponse
import com.app.mobile.data.api.models.task.UpdateTaskRequest
import kotlinx.serialization.json.JsonElement
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface AuthApiClient {

    @GET("auth/me")
    suspend fun getMe(): Response<UserMeApiResponse>

    @POST("auth/change/name")
    suspend fun updateName(@Body request: ChangeNameRequest): Response<ResponseApiModel>

    @DELETE("auth/logout")
    suspend fun logoutAccount(): Response<ResponseApiModel>

    @DELETE("auth/delete/user")
    suspend fun deleteAccount(): Response<ResponseApiModel>

    @POST("calcQueen/calc")
    suspend fun calcQueen(@Body calcQueenRequest: CalcQueenRequest): Response<QueenCalendarResponse>

    @POST("auth/fcm/update")
    suspend fun registerPushToken(@Body pushTokenCreation: PushTokenCreationModel): Response<ResponseApiModel>

    @GET("instructions/list")
    suspend fun getInstructionsList(): Response<JsonElement>

    // --- Hive ---

    @POST("hive/create")
    suspend fun createHive(@Body request: CreateHiveRequest): Response<ResponseApiModel>

    @GET("hive/list")
    suspend fun getHives(@Query("active") active: Boolean? = null): Response<HiveListResponse>

    @GET("hive/")
    suspend fun getHive(@Query("name") name: String): Response<HiveDetailsResponse>

    @PUT("hive/update")
    suspend fun updateHive(@Body request: UpdateHiveRequest): Response<ResponseApiModel>

    @HTTP(method = "DELETE", path = "hive/delete", hasBody = true)
    suspend fun deleteHive(@Body request: DeleteHiveRequest): Response<ResponseApiModel>

    @POST("hive/link/hub")
    suspend fun linkHubToHive(@Body request: LinkToHiveRequest): Response<ResponseApiModel>

    @POST("hive/link/queen")
    suspend fun linkQueenToHive(@Body request: LinkToHiveRequest): Response<ResponseApiModel>

    // --- Hub ---

    @POST("hub/create")
    suspend fun createHub(@Body request: CreateHubRequest): Response<ResponseApiModel>

    @GET("hub/list")
    suspend fun getHubs(): Response<HubListResponse>

    @GET("hub/")
    suspend fun getHub(@Query("id") id: String): Response<HubDetailsResponse>

    @PUT("hub/update")
    suspend fun updateHub(@Body request: UpdateHubRequest): Response<ResponseApiModel>

    @HTTP(method = "DELETE", path = "hub/delete", hasBody = true)
    suspend fun deleteHub(@Body request: DeleteHubRequest): Response<ResponseApiModel>

    // --- Telemetry ---

    @GET("telemetry/sensor/last")
    suspend fun getLastSensorReading(@Query("hub") hubId: String): Response<LastSensorReadingResponse>

    @GET("telemetry/temperature/get")
    suspend fun getTemperatureHistory(
        @Query("hub") hubId: String,
        @Query("since") since: Long? = null
    ): Response<TelemetryHistoryResponse>

    @GET("telemetry/noise/get")
    suspend fun getNoiseHistory(
        @Query("hub") hubId: String,
        @Query("since") since: Long? = null
    ): Response<TelemetryHistoryResponse>

    @GET("telemetry/weight/get")
    suspend fun getWeightHistory(
        @Query("hub") hubId: String,
        @Query("since") since: Long? = null
    ): Response<TelemetryHistoryResponse>

    @POST("telemetry/weight/set")
    suspend fun setWeight(@Body request: SetWeightRequest): Response<ResponseApiModel>

    // --- Queen ---

    @POST("queen/create")
    suspend fun createQueen(@Body request: CreateQueenRequest): Response<QueenDetailsResponse>

    @GET("queen/list")
    suspend fun getQueens(): Response<QueenListResponse>

    @GET("queen/")
    suspend fun getQueen(@Query("name") name: String): Response<QueenDetailsResponse>

    @PUT("queen/update")
    suspend fun updateQueen(@Body request: UpdateQueenRequest): Response<ResponseApiModel>

    @HTTP(method = "DELETE", path = "queen/delete", hasBody = true)
    suspend fun deleteQueen(@Body request: DeleteQueenRequest): Response<ResponseApiModel>

    // --- Tasks (Beehive Works) ---

    @POST("task/create")
    suspend fun createTask(@Body request: CreateTaskRequest): Response<TaskCreateResponse>

    @GET("task/list")
    suspend fun getTasks(@Query("hive_name") hiveName: String? = null): Response<TaskListResponse>

    @PUT("task/update")
    suspend fun updateTask(@Body request: UpdateTaskRequest): Response<ResponseApiModel>

    @HTTP(method = "DELETE", path = "task/delete", hasBody = true)
    suspend fun deleteTask(@Body request: DeleteTaskRequest): Response<ResponseApiModel>
}
