package com.app.mobile.domain.usecase.hives

import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.domain.models.hives.HiveDomainPreview
import com.app.mobile.domain.models.hives.HiveResult
import com.app.mobile.domain.models.hives.HubDomain
import com.app.mobile.domain.models.hives.queen.QueenDomainPreview
import com.app.mobile.domain.repository.datasource.HivesDataSource
import com.app.mobile.domain.repository.datasource.HubDataSource
import com.app.mobile.domain.repository.datasource.QueenDataSource
import com.app.mobile.domain.usecase.hives.hive.CreateHiveUseCase
import com.app.mobile.domain.usecase.hives.hive.DeleteHiveUseCase
import com.app.mobile.domain.usecase.hives.hive.GetHiveUseCase
import com.app.mobile.domain.usecase.hives.hive.GetHivesPreviewUseCase
import com.app.mobile.domain.usecase.hives.hub.GetHubsUseCase
import com.app.mobile.domain.usecase.hives.hub.GetHubWithSensorsUseCase
import com.app.mobile.domain.usecase.hives.hub.GetHubsWithSensorsUseCase
import com.app.mobile.domain.usecase.hives.queen.DeleteQueenUseCase
import com.app.mobile.domain.usecase.hives.queen.GetQueenUseCase
import com.app.mobile.domain.usecase.hives.queen.GetQueensUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HiveUseCasesTest {

    private val hivesDataSource = mockk<HivesDataSource>()
    private val hubDataSource = mockk<HubDataSource>()
    private val queenDataSource = mockk<QueenDataSource>()

    // region GetHiveUseCase

    @Test
    fun `GetHiveUseCase - delegates to dataSource with correct name`() = runTest {
        val hiveResult = HiveResult(name = "Hive1")
        coEvery { hivesDataSource.getHive("Hive1") } returns ApiResult.Success(hiveResult)

        val result = GetHiveUseCase(hivesDataSource).invoke("Hive1")

        assertTrue(result is ApiResult.Success)
        assertEquals("Hive1", (result as ApiResult.Success).data.name)
        coVerify(exactly = 1) { hivesDataSource.getHive("Hive1") }
    }

    @Test
    fun `GetHiveUseCase - propagates HttpError`() = runTest {
        coEvery { hivesDataSource.getHive(any()) } returns ApiResult.HttpError(404)

        val result = GetHiveUseCase(hivesDataSource).invoke("Missing")

        assertTrue(result is ApiResult.HttpError)
    }

    // endregion

    // region GetHivesPreviewUseCase

    @Test
    fun `GetHivesPreviewUseCase - returns list from dataSource`() = runTest {
        val previews = listOf(
            HiveDomainPreview(name = "H1"),
            HiveDomainPreview(name = "H2")
        )
        coEvery { hivesDataSource.getHives() } returns ApiResult.Success(previews)

        val result = GetHivesPreviewUseCase(hivesDataSource).invoke()

        assertTrue(result is ApiResult.Success)
        assertEquals(2, (result as ApiResult.Success).data.size)
    }

    // endregion

    // region CreateHiveUseCase

    @Test
    fun `CreateHiveUseCase - creates HiveEditorDomain with default name`() {
        val result = CreateHiveUseCase().invoke()

        assertEquals("Улей", result.name)
    }

    // endregion

    // region DeleteHiveUseCase

    @Test
    fun `DeleteHiveUseCase - delegates with correct name`() = runTest {
        coEvery { hivesDataSource.deleteHive("Hive1") } returns ApiResult.Success(Unit)

        val result = DeleteHiveUseCase(hivesDataSource).invoke("Hive1")

        assertTrue(result is ApiResult.Success)
        coVerify(exactly = 1) { hivesDataSource.deleteHive("Hive1") }
    }

    // endregion

    // region GetHubsUseCase

    @Test
    fun `GetHubsUseCase - returns hubs list`() = runTest {
        val hubs = listOf(HubDomain(id = "h1", name = "Hub 1"))
        coEvery { hubDataSource.getHubs() } returns ApiResult.Success(hubs)

        val result = GetHubsUseCase(hubDataSource).invoke()

        assertTrue(result is ApiResult.Success)
        assertEquals(1, (result as ApiResult.Success).data.size)
    }

    // endregion

    // region GetHubWithSensorsUseCase

    @Test
    fun `GetHubWithSensorsUseCase - delegates with correct hub id`() = runTest {
        val hub = HubDomain(id = "h1", name = "Hub 1")
        coEvery { hubDataSource.getHubWithSensors("h1") } returns ApiResult.Success(hub)

        val result = GetHubWithSensorsUseCase(hubDataSource).invoke("h1")

        assertTrue(result is ApiResult.Success)
        assertEquals("h1", (result as ApiResult.Success).data.id)
        coVerify(exactly = 1) { hubDataSource.getHubWithSensors("h1") }
    }

    // endregion

    // region GetHubsWithSensorsUseCase

    @Test
    fun `GetHubsWithSensorsUseCase - returns hubs with sensors`() = runTest {
        val hubs = listOf(HubDomain(id = "h1", name = "Hub 1"), HubDomain(id = "h2", name = "Hub 2"))
        coEvery { hubDataSource.getHubsWithSensors() } returns ApiResult.Success(hubs)

        val result = GetHubsWithSensorsUseCase(hubDataSource).invoke()

        assertTrue(result is ApiResult.Success)
        assertEquals(2, (result as ApiResult.Success).data.size)
    }

    // endregion

    // region GetQueenUseCase

    @Test
    fun `GetQueenUseCase - delegates with correct name`() = runTest {
        coEvery { queenDataSource.getQueen("Q1") } returns ApiResult.HttpError(404)

        val result = GetQueenUseCase(queenDataSource).invoke("Q1")

        assertTrue(result is ApiResult.HttpError)
        coVerify(exactly = 1) { queenDataSource.getQueen("Q1") }
    }

    // endregion

    // region GetQueensUseCase

    @Test
    fun `GetQueensUseCase - returns queens list`() = runTest {
        val queens = listOf(
            QueenDomainPreview(name = "Q1", startDate = java.time.LocalDate.of(2024, 1, 1)),
            QueenDomainPreview(name = "Q2", startDate = java.time.LocalDate.of(2024, 3, 1))
        )
        coEvery { queenDataSource.getQueens() } returns ApiResult.Success(queens)

        val result = GetQueensUseCase(queenDataSource).invoke()

        assertTrue(result is ApiResult.Success)
        assertEquals(2, (result as ApiResult.Success).data.size)
    }

    // endregion

    // region DeleteQueenUseCase

    @Test
    fun `DeleteQueenUseCase - delegates with correct name`() = runTest {
        coEvery { queenDataSource.deleteQueen("Q1") } returns ApiResult.Success(Unit)

        val result = DeleteQueenUseCase(queenDataSource).invoke("Q1")

        assertTrue(result is ApiResult.Success)
        coVerify(exactly = 1) { queenDataSource.deleteQueen("Q1") }
    }

    // endregion
}
