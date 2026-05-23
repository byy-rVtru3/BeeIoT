package com.app.mobile.domain.scenario

import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.domain.models.DateRange
import com.app.mobile.domain.models.hives.HiveDomain
import com.app.mobile.domain.models.hives.HiveResult
import com.app.mobile.domain.models.hives.HubDomain
import com.app.mobile.domain.models.hives.queen.AdultStage
import com.app.mobile.domain.models.hives.queen.EggStage
import com.app.mobile.domain.models.hives.queen.LarvaStage
import com.app.mobile.domain.models.hives.queen.PupaStage
import com.app.mobile.domain.models.hives.queen.QueenDomain
import com.app.mobile.domain.models.hives.queen.QueenLifecycle
import com.app.mobile.domain.usecase.hives.hive.GetHiveUseCase
import com.app.mobile.domain.usecase.hives.hub.GetHubWithSensorsUseCase
import com.app.mobile.domain.usecase.hives.queen.GetQueenUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class GetHiveScenarioTest {

    private val getHiveUseCase = mockk<GetHiveUseCase>()
    private val getHubWithSensorsUseCase = mockk<GetHubWithSensorsUseCase>()
    private val getQueenUseCase = mockk<GetQueenUseCase>()

    private val scenario = GetHiveScenario(getHiveUseCase, getHubWithSensorsUseCase, getQueenUseCase)

    @Test
    fun `success - hive with hub and queen returns fully populated HiveDomain`() = runTest {
        val hiveResult = HiveResult(name = "Hive1", hub = "hub-1", queen = "Queen1", active = true)
        val hub = HubDomain(id = "hub-1", name = "Hub 1")
        val queen = buildQueenDomain("Queen1")

        coEvery { getHiveUseCase("Hive1") } returns ApiResult.Success(hiveResult)
        coEvery { getHubWithSensorsUseCase("hub-1") } returns ApiResult.Success(hub)
        coEvery { getQueenUseCase("Queen1") } returns ApiResult.Success(queen)

        val result = scenario("Hive1")

        assertTrue(result is ApiResult.Success)
        val domain = (result as ApiResult.Success<HiveDomain>).data
        assertEquals("Hive1", domain.name)
        assertEquals("hub-1", domain.hub?.id)
        assertEquals("Queen1", domain.queen?.name)
        assertTrue(domain.active)
    }

    @Test
    fun `success - hive without hub and queen returns empty refs`() = runTest {
        val hiveResult = HiveResult(name = "Hive2", hub = null, queen = null, active = false)
        coEvery { getHiveUseCase("Hive2") } returns ApiResult.Success(hiveResult)

        val result = scenario("Hive2")

        assertTrue(result is ApiResult.Success)
        val domain = (result as ApiResult.Success<HiveDomain>).data
        assertNull(domain.hub)
        assertNull(domain.queen)
        coVerify(exactly = 0) { getHubWithSensorsUseCase(any()) }
        coVerify(exactly = 0) { getQueenUseCase(any()) }
    }

    @Test
    fun `success - hub API fails, hub is null in result`() = runTest {
        val hiveResult = HiveResult(name = "Hive3", hub = "hub-x", queen = null, active = true)
        coEvery { getHiveUseCase("Hive3") } returns ApiResult.Success(hiveResult)
        coEvery { getHubWithSensorsUseCase("hub-x") } returns ApiResult.HttpError(404)

        val result = scenario("Hive3")

        assertTrue(result is ApiResult.Success)
        assertNull((result as ApiResult.Success<HiveDomain>).data.hub)
    }

    @Test
    fun `success - queen API fails, queen is null in result`() = runTest {
        val hiveResult = HiveResult(name = "Hive4", hub = null, queen = "QueenX", active = true)
        coEvery { getHiveUseCase("Hive4") } returns ApiResult.Success(hiveResult)
        coEvery { getQueenUseCase("QueenX") } returns ApiResult.HttpError(404)

        val result = scenario("Hive4")

        assertTrue(result is ApiResult.Success)
        assertNull((result as ApiResult.Success<HiveDomain>).data.queen)
    }

    @Test
    fun `failure - hive API fails, returns error without fetching hub or queen`() = runTest {
        coEvery { getHiveUseCase("Missing") } returns ApiResult.HttpError(404)

        val result = scenario("Missing")

        assertTrue(result is ApiResult.HttpError)
        assertEquals(404, (result as ApiResult.HttpError).code)
        coVerify(exactly = 0) { getHubWithSensorsUseCase(any()) }
        coVerify(exactly = 0) { getQueenUseCase(any()) }
    }

    @Test
    fun `failure - network error propagates`() = runTest {
        val exception = RuntimeException("No network")
        coEvery { getHiveUseCase(any()) } returns ApiResult.NetworkError(exception)

        val result = scenario("Hive5")

        assertTrue(result is ApiResult.NetworkError)
    }

    private fun buildQueenDomain(name: String): QueenDomain {
        val base = LocalDate.of(2020, 1, 1)
        return QueenDomain(
            name = name,
            stages = QueenLifecycle(
                birthDate = base,
                egg = EggStage(base, base.plusDays(1), base.plusDays(2)),
                larva = LarvaStage(
                    hatchDate = base.plusDays(3),
                    feedingDays = listOf(base.plusDays(4), base.plusDays(5)),
                    sealedDate = base.plusDays(8)
                ),
                pupa = PupaStage(
                    period = DateRange(base.plusDays(8), base.plusDays(14)),
                    selectionDate = base.plusDays(10)
                ),
                adult = AdultStage(
                    emergence = DateRange(base.plusDays(14), base.plusDays(15)),
                    maturation = DateRange(base.plusDays(15), base.plusDays(21)),
                    matingFlight = DateRange(base.plusDays(21), base.plusDays(28)),
                    insemination = DateRange(base.plusDays(28), base.plusDays(35)),
                    checkLaying = DateRange(base.plusDays(35), base.plusDays(42))
                )
            )
        )
    }
}
