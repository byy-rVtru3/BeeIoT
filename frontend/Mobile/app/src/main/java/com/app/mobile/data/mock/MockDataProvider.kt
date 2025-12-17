package com.app.mobile.data.mock

import android.content.Context
import com.app.mobile.domain.models.UserDomain
import com.app.mobile.domain.models.DateRange
import com.app.mobile.domain.models.hives.queen.AdultStage
import com.app.mobile.domain.models.hives.queen.EggStage
import com.app.mobile.domain.models.hives.queen.LarvaStage
import com.app.mobile.domain.models.hives.queen.PupaStage
import com.app.mobile.domain.models.hives.queen.QueenLifecycle
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.IOException
import java.time.LocalDate


object MockDataProvider {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Volatile
    private var cachedUser: MockUserData? = null

    @Volatile
    private var cachedQueenLifecycle: MockQueenLifecycle? = null

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
    fun getQueenLifecycle(context: Context): QueenLifecycle {
        if (cachedQueenLifecycle == null) {
            val jsonString = loadJsonFromAssets(context, "mock_queen_lifecycle.json")
            cachedQueenLifecycle = json.decodeFromString<MockQueenLifecycle>(jsonString)
        }
        return cachedQueenLifecycle!!.toDomain()
    }

    @Synchronized
    fun clearCache() {
        cachedUser = null
        cachedQueenLifecycle = null
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

@Serializable
data class MockQueenLifecycle(
    val birthDate: String,
    val egg: MockEggStage,
    val larva: MockLarvaStage,
    val pupa: MockPupaStage,
    val adult: MockAdultStage
)

@Serializable
data class MockEggStage(
    val day0Standing: String,
    val day1Tilted: String,
    val day2Lying: String
)

@Serializable
data class MockLarvaStage(
    val hatchDate: String,
    val feedingDays: List<String>,
    val sealedDate: String
)

@Serializable
data class MockPupaStage(
    val period: MockDateRange,
    val selectionDate: String
)

@Serializable
data class MockAdultStage(
    val emergence: MockDateRange,
    val maturation: MockDateRange,
    val matingFlight: MockDateRange,
    val insemination: MockDateRange,
    val checkLaying: MockDateRange
)

@Serializable
data class MockDateRange(
    val start: String,
    val end: String
)

fun MockQueenLifecycle.toDomain() = QueenLifecycle(
    birthDate = LocalDate.parse(birthDate),
    egg = egg.toDomain(),
    larva = larva.toDomain(),
    pupa = pupa.toDomain(),
    adult = adult.toDomain()
)

fun MockEggStage.toDomain() = EggStage(
    day0Standing = LocalDate.parse(day0Standing),
    day1Tilted = LocalDate.parse(day1Tilted),
    day2Lying = LocalDate.parse(day2Lying)
)

fun MockLarvaStage.toDomain() = LarvaStage(
    hatchDate = LocalDate.parse(hatchDate),
    feedingDays = feedingDays.map { LocalDate.parse(it) },
    sealedDate = LocalDate.parse(sealedDate)
)

fun MockPupaStage.toDomain() = PupaStage(
    period = period.toDomain(),
    selectionDate = LocalDate.parse(selectionDate)
)

fun MockAdultStage.toDomain() = AdultStage(
    emergence = emergence.toDomain(),
    maturation = maturation.toDomain(),
    matingFlight = matingFlight.toDomain(),
    insemination = insemination.toDomain(),
    checkLaying = checkLaying.toDomain()
)

fun MockDateRange.toDomain() = DateRange(
    start = LocalDate.parse(start),
    end = LocalDate.parse(end)
)
