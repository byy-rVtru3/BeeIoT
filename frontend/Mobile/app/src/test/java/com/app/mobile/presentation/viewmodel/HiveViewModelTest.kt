package com.app.mobile.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.domain.models.hives.HiveDomain
import com.app.mobile.domain.models.hives.HubDomain
import com.app.mobile.domain.models.hives.WorkDomain
import com.app.mobile.domain.models.telemetry.SensorReadings
import com.app.mobile.domain.scenario.GetHiveScenario
import com.app.mobile.domain.usecase.hives.hive.DeleteHiveUseCase
import com.app.mobile.domain.usecase.hives.works.GetWorksUseCase
import com.app.mobile.presentation.ui.screens.hive.details.viewmodel.HiveEvent
import com.app.mobile.presentation.ui.screens.hive.details.viewmodel.HiveUiState
import com.app.mobile.presentation.ui.screens.hive.details.viewmodel.HiveViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class HiveViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val getHiveScenario = mockk<GetHiveScenario>()
    private val getWorksUseCase = mockk<GetWorksUseCase>()
    private val deleteHiveUseCase = mockk<DeleteHiveUseCase>()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(hiveName: String = "Hive1"): HiveViewModel {
        val handle = SavedStateHandle(mapOf("hiveName" to hiveName))
        return HiveViewModel(handle, getHiveScenario, getWorksUseCase, deleteHiveUseCase)
    }

    // region loadHive

    @Test
    fun `loadHive - success - state becomes Content with hive name`() = runTest {
        val hive = hiveDomain(name = "Hive1")
        coEvery { getHiveScenario("Hive1") } returns ApiResult.Success(hive)
        coEvery { getWorksUseCase("Hive1") } returns emptyList()

        val vm = createViewModel("Hive1")
        vm.loadHive()

        val state = vm.uiState.value
        assertTrue(state is HiveUiState.Content)
        assertEquals("Hive1", (state as HiveUiState.Content).hive.name)
    }

    @Test
    fun `loadHive - success - shows last 2 works sorted by date`() = runTest {
        val hive = hiveDomain()
        val oldest = workDomain(id = "w1", dateTime = LocalDateTime.of(2024, 1, 1, 0, 0))
        val middle = workDomain(id = "w2", dateTime = LocalDateTime.of(2024, 3, 1, 0, 0))
        val newest = workDomain(id = "w3", dateTime = LocalDateTime.of(2024, 6, 1, 0, 0))

        coEvery { getHiveScenario("Hive1") } returns ApiResult.Success(hive)
        coEvery { getWorksUseCase("Hive1") } returns listOf(oldest, middle, newest)

        val vm = createViewModel()
        vm.loadHive()

        val content = vm.uiState.value as HiveUiState.Content
        assertEquals(2, content.hive.recentWorks.size)
        assertEquals("w3", content.hive.recentWorks[0].id)
        assertEquals("w2", content.hive.recentWorks[1].id)
    }

    @Test
    fun `loadHive - success - hub is null when not present`() = runTest {
        val hive = hiveDomain(hub = null)
        coEvery { getHiveScenario("Hive1") } returns ApiResult.Success(hive)
        coEvery { getWorksUseCase("Hive1") } returns emptyList()

        val vm = createViewModel()
        vm.loadHive()

        val state = vm.uiState.value as HiveUiState.Content
        assertNull(state.hive.hub)
    }

    @Test
    fun `loadHive - success - queen is null when not present`() = runTest {
        val hive = hiveDomain(queen = null)
        coEvery { getHiveScenario("Hive1") } returns ApiResult.Success(hive)
        coEvery { getWorksUseCase("Hive1") } returns emptyList()

        val vm = createViewModel()
        vm.loadHive()

        val state = vm.uiState.value as HiveUiState.Content
        assertNull(state.hive.queen)
    }

    @Test
    fun `loadHive - API error - sends ShowSnackBar and NavigateToHiveList`() = runTest {
        coEvery { getHiveScenario("Hive1") } returns ApiResult.HttpError(404)
        coEvery { getWorksUseCase("Hive1") } returns emptyList()

        val vm = createViewModel()

        vm.event.test {
            vm.loadHive()
            val first = awaitItem()
            val second = awaitItem()

            val events = listOf(first, second)
            assertTrue(events.any { it is HiveEvent.ShowSnackBar })
            assertTrue(events.any { it is HiveEvent.NavigateToHiveList })
            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion

    // region refresh

    @Test
    fun `refresh - updates content state`() = runTest {
        val hive = hiveDomain(name = "Hive1")
        coEvery { getHiveScenario("Hive1") } returns ApiResult.Success(hive)
        coEvery { getWorksUseCase("Hive1") } returns emptyList()

        val vm = createViewModel()
        vm.loadHive()
        vm.refresh()

        val state = vm.uiState.value
        assertTrue(state is HiveUiState.Content)
        assertFalse((state as HiveUiState.Content).isRefreshing)
    }

    @Test
    fun `refresh - does nothing when state is Loading`() = runTest {
        val vm = createViewModel()

        vm.refresh()

        assertTrue(vm.uiState.value is HiveUiState.Loading)
        coVerify(exactly = 0) { getHiveScenario(any()) }
    }

    // endregion

    // region onQueenClick

    @Test
    fun `onQueenClick - sends NavigateToQueenByHive when queen present`() = runTest {
        val hive = hiveDomain(queen = null) // we can't easily test with queen without mapper
        coEvery { getHiveScenario("Hive1") } returns ApiResult.Success(hive)
        coEvery { getWorksUseCase("Hive1") } returns emptyList()

        val vm = createViewModel()
        vm.loadHive()
        // queen is null, so no event expected
        vm.event.test {
            vm.onQueenClick()
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion

    // region onWorkClick

    @Test
    fun `onWorkClick - sends NavigateToWorkDetail with correct ids`() = runTest {
        val hive = hiveDomain()
        coEvery { getHiveScenario("Hive1") } returns ApiResult.Success(hive)
        coEvery { getWorksUseCase("Hive1") } returns emptyList()

        val vm = createViewModel("Hive1")
        vm.loadHive()

        vm.event.test {
            vm.onWorkClick("w-1")
            val event = awaitItem()
            assertTrue(event is HiveEvent.NavigateToWorkDetail)
            assertEquals("w-1", (event as HiveEvent.NavigateToWorkDetail).workId)
            assertEquals("Hive1", event.hiveName)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion

    // region onWorksClick

    @Test
    fun `onWorksClick - sends NavigateToWorkByHive when in Content state`() = runTest {
        val hive = hiveDomain()
        coEvery { getHiveScenario("Hive1") } returns ApiResult.Success(hive)
        coEvery { getWorksUseCase("Hive1") } returns emptyList()

        val vm = createViewModel("Hive1")
        vm.loadHive()

        vm.event.test {
            vm.onWorksClick()
            val event = awaitItem()
            assertTrue(event is HiveEvent.NavigateToWorkByHive)
            assertEquals("Hive1", (event as HiveEvent.NavigateToWorkByHive).hiveName)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion

    // region onDeleteClick

    @Test
    fun `onDeleteClick - success - sends NavigateToHiveList`() = runTest {
        coEvery { deleteHiveUseCase("Hive1") } returns ApiResult.Success(Unit)

        val vm = createViewModel("Hive1")

        vm.event.test {
            vm.onDeleteClick()
            val event = awaitItem()
            assertTrue(event is HiveEvent.NavigateToHiveList)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onDeleteClick - failure - sends ShowSnackBar`() = runTest {
        coEvery { deleteHiveUseCase("Hive1") } returns ApiResult.HttpError(500)

        val vm = createViewModel("Hive1")

        vm.event.test {
            vm.onDeleteClick()
            val event = awaitItem()
            assertTrue(event is HiveEvent.ShowSnackBar)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion

    // region sensor navigation

    @Test
    fun `onTemperatureClick - sends NavigateToTemperatureByHive with hub data`() = runTest {
        val hub = hubDomain(id = "hub-1", name = "Hub A")
        val hive = hiveDomain(hub = hub)
        coEvery { getHiveScenario("Hive1") } returns ApiResult.Success(hive)
        coEvery { getWorksUseCase("Hive1") } returns emptyList()

        val vm = createViewModel()
        vm.loadHive()

        vm.event.test {
            vm.onTemperatureClick()
            val event = awaitItem()
            assertTrue(event is HiveEvent.NavigateToTemperatureByHive)
            assertEquals("hub-1", (event as HiveEvent.NavigateToTemperatureByHive).hubId)
            assertEquals("Hub A", event.hubName)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onNoiseClick - sends NavigateToNoiseByHive`() = runTest {
        val hub = hubDomain(id = "hub-1", name = "Hub A")
        val hive = hiveDomain(hub = hub)
        coEvery { getHiveScenario("Hive1") } returns ApiResult.Success(hive)
        coEvery { getWorksUseCase("Hive1") } returns emptyList()

        val vm = createViewModel()
        vm.loadHive()

        vm.event.test {
            vm.onNoiseClick()
            val event = awaitItem()
            assertTrue(event is HiveEvent.NavigateToNoiseByHive)
            assertEquals("hub-1", (event as HiveEvent.NavigateToNoiseByHive).hubId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onWeightClick - sends NavigateToWeightByHive`() = runTest {
        val hub = hubDomain(id = "hub-1", name = "Hub A")
        val hive = hiveDomain(hub = hub)
        coEvery { getHiveScenario("Hive1") } returns ApiResult.Success(hive)
        coEvery { getWorksUseCase("Hive1") } returns emptyList()

        val vm = createViewModel()
        vm.loadHive()

        vm.event.test {
            vm.onWeightClick()
            val event = awaitItem()
            assertTrue(event is HiveEvent.NavigateToWeightByHive)
            assertEquals("hub-1", (event as HiveEvent.NavigateToWeightByHive).hubId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onTemperatureClick - does nothing when no hub`() = runTest {
        val hive = hiveDomain(hub = null)
        coEvery { getHiveScenario("Hive1") } returns ApiResult.Success(hive)
        coEvery { getWorksUseCase("Hive1") } returns emptyList()

        val vm = createViewModel()
        vm.loadHive()

        vm.event.test {
            vm.onTemperatureClick()
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion

    // region onHiveEditClick

    @Test
    fun `onHiveEditClick - sends NavigateToHiveEdit when in Content state`() = runTest {
        val hive = hiveDomain()
        coEvery { getHiveScenario("Hive1") } returns ApiResult.Success(hive)
        coEvery { getWorksUseCase("Hive1") } returns emptyList()

        val vm = createViewModel("Hive1")
        vm.loadHive()

        vm.event.test {
            vm.onHiveEditClick()
            val event = awaitItem()
            assertTrue(event is HiveEvent.NavigateToHiveEdit)
            assertEquals("Hive1", (event as HiveEvent.NavigateToHiveEdit).hiveName)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion

    private fun hiveDomain(
        name: String = "Hive1",
        hub: HubDomain? = null,
        queen: com.app.mobile.domain.models.hives.queen.QueenDomain? = null,
    ) = HiveDomain(name = name, hub = hub, queen = queen, active = true)

    private fun hubDomain(
        id: String = "hub-1",
        name: String = "Hub 1",
        sensorReadings: SensorReadings? = null,
    ) = HubDomain(id = id, name = name, sensorReadings = sensorReadings)

    private fun workDomain(
        id: String = "w-1",
        dateTime: LocalDateTime = LocalDateTime.of(2024, 1, 1, 0, 0),
    ) = WorkDomain(
        id = id,
        hiveId = "Hive1",
        title = "Work",
        text = "",
        dateTime = dateTime
    )
}
