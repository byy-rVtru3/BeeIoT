package com.app.mobile.data.mock.interceptor.methods

import android.content.Context
import com.app.mobile.data.mock.interceptor.buildErrorResponse
import com.app.mobile.data.mock.interceptor.buildSuccessResponse
import com.app.mobile.data.mock.interceptor.getResourceIdByName
import com.app.mobile.data.mock.interceptor.readJsonFromRaw
import okhttp3.Request
import okhttp3.Response

fun handleDeleteRequest(context: Context, path: String, request: Request): Response {
    return when {
        path.endsWith("/auth/logout") -> {
            val resourceId = context.getResourceIdByName("delete_auth_logout")
            val json = context.readJsonFromRaw(resourceId)
            buildSuccessResponse(request, json)
        }
        path.endsWith("/auth/delete/user") -> {
            val resourceId = context.getResourceIdByName("delete_auth_delete_user")
            val json = context.readJsonFromRaw(resourceId)
            buildSuccessResponse(request, json)
        }
        else -> buildErrorResponse(request, "NOT API")
    }
}
