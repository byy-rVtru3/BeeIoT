package com.app.mobile.data.api.models

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class HttpError(val code: Int, val errorBody: String? = null) : ApiResult<Nothing>()
    data class NetworkError(val exception: Throwable) : ApiResult<Nothing>()
    data class UnexpectedError(val exception: Throwable) : ApiResult<Nothing>()
}