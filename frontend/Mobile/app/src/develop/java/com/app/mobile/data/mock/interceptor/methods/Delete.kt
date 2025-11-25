package com.app.mobile.data.mock.interceptor.methods

import android.content.Context
import com.app.mobile.data.mock.interceptor.buildErrorResponse
import okhttp3.Request
import okhttp3.Response

fun handleDeleteRequest(context: Context, path: String, request: Request): Response {
    return when {
        // Добавьте сюда DELETE endpoints при необходимости
        else -> buildErrorResponse(request, "NOT API")
    }
}

