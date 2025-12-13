package com.app.mobile.data.mock

import android.content.Context
import com.app.mobile.domain.models.UserDomain
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.IOException


object MockDataProvider {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Volatile
    private var cachedUser: MockUserData? = null

    private fun loadJsonFromAssets(context: Context, fileName: String): String {
        return try {
            context.assets.open("mock_data/$fileName").bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            throw IOException("Failed to load mock data file: $fileName", e)
        }
    }

    @Synchronized
    fun getUser(context: Context): MockUserData {
        if (cachedUser == null) {
            val jsonString = loadJsonFromAssets(context, "mock_user.json")
            cachedUser = json.decodeFromString<MockUserData>(jsonString)
        }
        return cachedUser ?: MockUserData(0, "", "", "", "")
    }

    fun getUserDomain(context: Context): UserDomain {
        return getUser(context).toDomain()
    }

    @Synchronized
    fun clearCache() {
        cachedUser = null
    }
}

@Serializable
data class MockUserData(
    val id: Int,
    val name: String,
    val email: String,
    val password: String,
    val jwtToken: String
)

fun MockUserData.toDomain() = UserDomain(
    name = name,
    email = email,
    password = password,
    jwtToken = jwtToken
)