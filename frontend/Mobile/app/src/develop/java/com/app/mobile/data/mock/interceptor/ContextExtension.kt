package com.app.mobile.data.mock.interceptor

import android.content.Context

fun Context.readJsonFromRaw(resourceId: Int): String {
    return try {
        resources.openRawResource(resourceId).bufferedReader().use { reader ->
            reader.readText()
        }
    } catch (e: Exception) {
        """{"error": "Failed to read mock data: ${e.message}"}"""
    }
}

fun Context.getResourceIdByName(resourceName: String): Int {
    return resources.getIdentifier(resourceName, "raw", packageName)
}
