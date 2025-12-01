package com.app.mobile.data.mock.interceptor.methods

import android.content.Context
import com.app.mobile.data.mock.interceptor.buildErrorResponse
import okhttp3.Request
import okhttp3.Response

fun handleDeleteRequest(context: Context, path: String, request: Request): Response {
    return when {
        path.endsWith("/auth/logout") ->
            handleMockResponse(context, request, "delete_auth_logout")

        path.endsWith("/auth/delete/user") ->
            handleMockResponse(context, request, "delete_auth_delete_user")

        else -> buildErrorResponse(request, "NOT API")
    }
}
