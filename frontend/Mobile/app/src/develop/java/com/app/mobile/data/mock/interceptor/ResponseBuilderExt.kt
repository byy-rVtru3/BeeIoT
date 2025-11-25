package com.app.mobile.data.mock.interceptor

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

fun buildSuccessResponse(request: Request, jsonBody: String): Response {
    return Response.Builder()
        .code(200)
        .message("OK")
        .request(request)
        .protocol(Protocol.HTTP_1_1)
        .body(jsonBody.toResponseBody("application/json".toMediaTypeOrNull()))
        .addHeader("content-type", "application/json")
        .build()
}

fun buildErrorResponse(request: Request, errorMessage: String): Response {
    val errorJson = """{"error": "$errorMessage"}"""
    return Response.Builder()
        .code(404)
        .message("Not Found")
        .request(request)
        .protocol(Protocol.HTTP_1_1)
        .body(errorJson.toResponseBody("application/json".toMediaTypeOrNull()))
        .addHeader("content-type", "application/json")
        .build()
}

