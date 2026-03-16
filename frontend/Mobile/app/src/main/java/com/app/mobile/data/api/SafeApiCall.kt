package com.app.mobile.data.api

import com.app.mobile.data.api.models.ApiResult
import retrofit2.Response
import java.io.IOException

suspend fun <T, R> safeApiCall(
    apiCall: suspend () -> Response<T>,
    onSuccess: (T) -> R
): ApiResult<R> {
    return try {
        val response = apiCall()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                ApiResult.Success(onSuccess(body))
            } else {
                ApiResult.UnexpectedError(IllegalStateException("Response body is null"))
            }
        } else {
            ApiResult.HttpError(
                code = response.code(),
                errorBody = response.errorBody()?.string()
            )
        }
    } catch (e: IOException) {
        ApiResult.NetworkError(e)
    } catch (e: Exception) {
        ApiResult.UnexpectedError(e)
    }
}

suspend fun <T> safeApiCall(
    apiCall: suspend () -> Response<T>
): ApiResult<Unit> {
    return try {
        val response = apiCall()
        if (response.isSuccessful) {
            ApiResult.Success(Unit)
        } else {
            ApiResult.HttpError(
                code = response.code(),
                errorBody = response.errorBody()?.string()
            )
        }
    } catch (e: IOException) {
        ApiResult.NetworkError(e)
    } catch (e: Exception) {
        ApiResult.UnexpectedError(e)
    }
}