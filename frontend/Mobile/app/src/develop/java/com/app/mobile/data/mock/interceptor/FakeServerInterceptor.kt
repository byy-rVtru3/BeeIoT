package com.app.mobile.data.mock.interceptor

import android.content.Context
import android.util.Log
import com.app.mobile.data.mock.interceptor.methods.handleDeleteRequest
import com.app.mobile.data.mock.interceptor.methods.handleGetRequest
import com.app.mobile.data.mock.interceptor.methods.handlePatchRequest
import com.app.mobile.data.mock.interceptor.methods.handlePostRequest
import com.app.mobile.data.mock.interceptor.methods.handlePutRequest
import okhttp3.Interceptor
import okhttp3.Response

class FakeServerInterceptor(private val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath
        val method = request.method

        Log.d("FakeServerInterceptor", "Intercepted: $method $path")

        return when (method) {
            "GET" -> handleGetRequest(context, path, request)
            "POST" -> handlePostRequest(context, path, request)
            "PUT" -> handlePutRequest(context, path, request)
            "PATCH" -> handlePatchRequest(context, path, request)
            "DELETE" -> handleDeleteRequest(context, path, request)
            else -> buildErrorResponse(request, "Method not supported")
        }
    }
}

