package com.app.mobile.data.mock.interceptor.methods

import android.content.Context
import com.app.mobile.data.mock.interceptor.buildErrorResponse
import com.app.mobile.data.mock.interceptor.buildSuccessResponse
import com.app.mobile.data.mock.interceptor.getResourceIdByName
import com.app.mobile.data.mock.interceptor.readJsonFromRaw
import okhttp3.Request
import okhttp3.Response

/**
 * Общая helper-функция для обработки mock-запросов с проверкой существования ресурса
 *
 * Используется во всех HTTP методах (GET, POST, PUT, DELETE, PATCH) для:
 * - Проверки существования JSON-файла в res/raw/
 * - Чтения содержимого файла
 * - Обработки ошибок
 *
 * @param context Контекст приложения для доступа к ресурсам
 * @param request Исходный HTTP запрос
 * @param resourceName Имя ресурса без расширения (например, "post_auth_login")
 * @return Response с данными из JSON или с ошибкой
 */
fun handleMockResponse(
    context: Context,
    request: Request,
    resourceName: String
): Response {
    val resourceId = context.getResourceIdByName(resourceName)

    if (resourceId == 0) {
        return buildErrorResponse(
            request,
            "Mock resource not found: $resourceName.json. Please add this file to res/raw/"
        )
    }

    return try {
        val json = context.readJsonFromRaw(resourceId)
        buildSuccessResponse(request, json)
    } catch (e: Exception) {
        buildErrorResponse(
            request,
            "Failed to read mock resource: $resourceName.json. Error: ${e.message}"
        )
    }
}

