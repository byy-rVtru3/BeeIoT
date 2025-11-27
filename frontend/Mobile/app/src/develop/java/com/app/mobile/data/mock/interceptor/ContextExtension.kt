package com.app.mobile.data.mock.interceptor

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

fun Context.readJsonFromRaw(resourceId: Int): String {
    return try {
        val inputStream = resources.openRawResource(resourceId)
        val reader = BufferedReader(InputStreamReader(inputStream))
        val stringBuilder = StringBuilder()
        var line: String?

        while (reader.readLine().also { line = it } != null) {
            stringBuilder.append(line)
        }

        reader.close()
        stringBuilder.toString()
    } catch (e: Exception) {
        """{"error": "Failed to read mock data: ${e.message}"}"""
    }
}

fun Context.getResourceIdByName(resourceName: String): Int {
    return resources.getIdentifier(resourceName, "raw", packageName)
}

