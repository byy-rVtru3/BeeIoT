package com.app.mobile.data.mock.interceptor.methods

import android.content.Context
import com.app.mobile.data.mock.interceptor.buildErrorResponse
import com.app.mobile.data.mock.interceptor.buildSuccessResponse
import com.app.mobile.data.mock.interceptor.getResourceIdByName
import com.app.mobile.data.mock.interceptor.readJsonFromRaw
import okhttp3.Request
import okhttp3.Response

fun handlePostRequest(context: Context, path: String, request: Request): Response {
    return when {
        path.endsWith("/auth/registration") -> {
            val resourceId = context.getResourceIdByName("post_auth_registration")
            val json = context.readJsonFromRaw(resourceId)
            buildSuccessResponse(request, json)
        }
        path.endsWith("/auth/confirm/registration") -> {
            val resourceId = context.getResourceIdByName("post_auth_confirm_registration")
            val json = context.readJsonFromRaw(resourceId)
            buildSuccessResponse(request, json)
        }
        path.endsWith("/auth/confirm/password") -> {
            val resourceId = context.getResourceIdByName("post_auth_confirm_password")
            val json = context.readJsonFromRaw(resourceId)
            buildSuccessResponse(request, json)
        }
        path.endsWith("/auth/login") -> {
            val resourceId = context.getResourceIdByName("post_auth_login")
            val json = context.readJsonFromRaw(resourceId)
            buildSuccessResponse(request, json)
        }
        else -> buildErrorResponse(request, "NOT API")
    }
}

