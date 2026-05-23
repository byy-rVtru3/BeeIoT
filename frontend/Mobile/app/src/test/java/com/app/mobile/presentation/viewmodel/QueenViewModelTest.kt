package com.app.mobile.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.domain.models.DateRange
import com.app.mobile.domain.models.hives.queen.AdultStage
import com.app.mobile.domain.models.hives.queen.EggStage
import com.app.mobile.domain.models.hives.queen.LarvaStage
import com.app.mobile.domain.models.hives.queen.PupaStage
import com.app.mobile.domain.models.hives.queen.QueenDomain
import com.app.mobile.domain.models.hives.queen.QueenLifecycle
import com.app.mobile.domain.usecase.hives.queen.DeleteQueenUseCase
import com.app.mobile.domain.usecase.hives.queen.GetQueenUseCase
import com.app.mobile.presentation.ui.screens.queen.details.viewmodel.QueenEvent
import com.app.mobile.presentation.ui.screens.queen.details.viewmodel.QueenUiState
import com.app.mobile.presentation.ui.screens.queen.details.viewmodel.QueenViewModel
import io.mockk.coEvery
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
import java.time.LocalDate
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class QueenViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val getQueenUseCase = mockk<GetQueenUseCase>()
    private val deleteQueenUseCase = mockk<DeleteQueenUseCase>()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        queenName: String = "Queen1",
        fromHiveName: String? = "Hive1",
    ): QueenViewModel {
        val map = mutableMapOf<String, Any?>("queenName" to queenName)
        if (fromHiveName != null) map["fromHiveName"] = fromHiveName
        val handle = SavedStateHandle(map)
        return QueenViewModel(handle, getQueenUseCase, deleteQueenUseCase)
    }

    // region getQueen

    @Test
    fun `getQueen - success - state becomes Content with queen name`() = runTest {
        val queen = buildQueen("Queen1")
        coEvery { getQueenUseCase("Queen1") } returns ApiResult.Success(queen)

        val vm = createViewModel()
        vm.getQueen()

        val state = vm.uiState.value
        assertTrue(state is QueenUiState.Content)
        assertEquals("Queen1", (state as QueenUiState.Content).queen.name)
    }

    @Test
    fun `getQueen - success - fromHiveName is preserved in Content state`() = runTest {
        val queen = buildQueen("Queen1")
        coEvery { getQueenUseCase("Queen1") } returns ApiResult.Success(queen)

        val vm = createViewModel(fromHiveName = "Hive1")
        vm.getQueen()

        val content = vm.uiState.value as QueenUiState.Content
        assertEquals("Hive1", content.fromHiveName)
    }

    @Test
    fun `getQueen - success - fromHiveName is null when not provided`() = runTest {
        val queen = buildQueen("Queen1")
        coEvery { getQueenUseCase("Queen1") } returns ApiResult.Success(queen)

        val vm = createViewModel(fromHiveName = null)
        vm.getQueen()

        val content = vm.uiState.value as QueenUiState.Content
        assertNull(content.fromHiveName)
    }

    @Test
    fun `getQueen - API error - sends ShowSnackBar then NavigateBack`() = runTest {
        coEvery { getQueenUseCase("Queen1") } returns ApiResult.HttpError(404)

        val vm = createViewModel()

        vm.event.test {
            vm.getQueen()
            val first = awaitItem()
            val second = awaitItem()
            val events = listOf(first, second)
            assertTrue(events.any { it is QueenEvent.ShowSnackBar })
            assertTrue(events.any { it is QueenEvent.NavigateBack })
            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion

    // region refresh

    @Test
    fun `refresh - updates Content state`() = runTest {
        val queen = buildQueen("Queen1")
        coEvery { getQueenUseCase("Queen1") } returns ApiResult.Success(queen)
        val vm = createViewModel()
        vm.getQueen()

        vm.refresh()

        val state = vm.uiState.value
        assertTrue(state is QueenUiState.Content)
        assertFalse((state as QueenUiState.Content).isRefreshing)
    }

    @Test
    fun `refresh - does nothing when state is Loading`() = runTest {
        val vm = createViewModel()
        // State is Loading

        vm.refresh()

        assertTrue(vm.uiState.value is QueenUiState.Loading)
    }

    @Test
    fun `refresh - error shows snackbar and keeps previous content`() = runTest {
        val queen = buildQueen("Queen1")
        coEvery { getQueenUseCase("Queen1") } returns ApiResult.Success(queen)
        val vm = createViewModel()
        vm.getQueen()

        coEvery { getQueenUseCase("Queen1") } returns ApiResult.HttpError(500)

        vm.event.test {
            vm.refresh()
            val event = awaitItem()
            assertTrue(event is QueenEvent.ShowSnackBar)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion

    // region onEditQueenClick

    @Test
    fun `onEditQueenClick - sends NavigateToEditQueen when in Content`() = runTest {
        val queen = buildQueen("Queen1")
        coEvery { getQueenUseCase("Queen1") } returns ApiResult.Success(queen)
        val vm = createViewModel()
        vm.getQueen()

        vm.event.test {
            vm.onEditQueenClick()
            val event = awaitItem()
            assertTrue(event is QueenEvent.NavigateToEditQueen)
            assertEquals("Queen1", (event as QueenEvent.NavigateToEditQueen).queenName)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onEditQueenClick - does nothing when state is Loading`() = runTest {
        val vm = createViewModel()

        vm.event.test {
            vm.onEditQueenClick()
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion

    // region onHiveClick

    @Test
    fun `onHiveClick - sends NavigateToHive with hiveName when fromHiveName present`() = runTest {
        val queen = buildQueen("Queen1")
        coEvery { getQueenUseCase("Queen1") } returns ApiResult.Success(queen)
        val vm = createViewModel(fromHiveName = "Hive1")
        vm.getQueen()

        vm.event.test {
            vm.onHiveClick()
            val event = awaitItem()
            assertTrue(event is QueenEvent.NavigateToHive)
            assertEquals("Hive1", (event as QueenEvent.NavigateToHive).hiveName)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onHiveClick - does nothing when fromHiveName is null`() = runTest {
        val queen = buildQueen("Queen1")
        coEvery { getQueenUseCase("Queen1") } returns ApiResult.Success(queen)
        val vm = createViewModel(fromHiveName = null)
        vm.getQueen()

        vm.event.test {
            vm.onHiveClick()
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion

    // region onDeleteClick

    @Test
    fun `onDeleteClick - success - sends NavigateBack`() = runTest {
        coEvery { deleteQueenUseCase("Queen1") } returns ApiResult.Success(Unit)
        val vm = createViewModel()

        vm.event.test {
            vm.onDeleteClick()
            val event = awaitItem()
            assertTrue(event is QueenEvent.NavigateBack)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onDeleteClick - failure - sends ShowSnackBar`() = runTest {
        coEvery { deleteQueenUseCase("Queen1") } returns ApiResult.HttpError(403)
        val vm = createViewModel()

        vm.event.test {
            vm.onDeleteClick()
            val event = awaitItem()
            assertTrue(event is QueenEvent.ShowSnackBar)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion

    // region resetError

    @Test
    fun `resetError - retries getQueen`() = runTest {
        coEvery { getQueenUseCase("Queen1") } returns ApiResult.HttpError(500)
        val vm = createViewModel()
        vm.getQueen()

        val queen = buildQueen("Queen1")
        coEvery { getQueenUseCase("Queen1") } returns ApiResult.Success(queen)
        vm.resetError()

        assertTrue(vm.uiState.value is QueenUiState.Content)
    }

    // endregion

    private fun buildQueen(name: String = "Queen1"): QueenDomain {
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
