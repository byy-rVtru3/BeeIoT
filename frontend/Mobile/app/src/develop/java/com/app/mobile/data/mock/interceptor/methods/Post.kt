package com.app.mobile.data.mock.interceptor.methods

import android.content.Context
import com.app.mobile.data.mock.interceptor.buildErrorResponse
import okhttp3.Request
import okhttp3.Response

fun handlePostRequest(context: Context, path: String, request: Request): Response {
    return when {
        path.endsWith("/auth/registration") ->
            handleMockResponse(context, request, "post_auth_registration")

        path.endsWith("/auth/confirm/registration") ->
            handleMockResponse(context, request, "post_auth_confirm_registration")

        path.endsWith("/auth/confirm/password") ->
            handleMockResponse(context, request, "post_auth_confirm_password")

        path.endsWith("/auth/login") ->
            handleMockResponse(context, request, "post_auth_login")

        else -> buildErrorResponse(request, "NOT API")
    }
}
