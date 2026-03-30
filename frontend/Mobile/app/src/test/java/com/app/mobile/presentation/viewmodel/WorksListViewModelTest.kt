package com.app.mobile.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.app.mobile.domain.models.hives.WorkDomain
import com.app.mobile.domain.usecase.hives.works.GetWorksUseCase
import com.app.mobile.presentation.ui.screens.works.list.viewmodel.WorksListEvent
import com.app.mobile.presentation.ui.screens.works.list.viewmodel.WorksListUiState
import com.app.mobile.presentation.ui.screens.works.list.viewmodel.WorksListViewModel
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class WorksListViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val getWorksUseCase = mockk<GetWorksUseCase>()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(hiveId: String = "h-1"): WorksListViewModel {
        val handle = SavedStateHandle(mapOf("hiveId" to hiveId))
        return WorksListViewModel(handle, getWorksUseCase)
    }

    // region loadWorks

    @Test
    fun `loadWorks - success - state becomes Content with mapped works`() = runTest {
        val works = listOf(work("w-1", "Осмотр"), work("w-2", "Кормление"))
        coEvery { getWorksUseCase("h-1") } returns works

        val vm = createViewModel()
        vm.loadWorks()

        val state = vm.uiState.value
        assertTrue(state is WorksListUiState.Content)
        val content = state as WorksListUiState.Content
        assertEquals(2, content.works.size)
        assertEquals("Осмотр", content.works[0].title)
        assertEquals("Кормление", content.works[1].title)
    }

    @Test
    fun `loadWorks - empty list - Content with empty works`() = runTest {
        coEvery { getWorksUseCase("h-1") } returns emptyList()

        val vm = createViewModel()
        vm.loadWorks()

        val state = vm.uiState.value as WorksListUiState.Content
        assertTrue(state.works.isEmpty())
    }

    @Test
    fun `loadWorks - exception - state becomes Error`() = runTest {
        coEvery { getWorksUseCase(any()) } throws RuntimeException("load failed")

        val vm = createViewModel()
        vm.loadWorks()

        val state = vm.uiState.value
        assertTrue(state is WorksListUiState.Error)
        assertEquals("load failed", (state as WorksListUiState.Error).message)
    }

    // endregion

    // region refresh

    @Test
    fun `refresh - updates Content state with fresh data`() = runTest {
        coEvery { getWorksUseCase("h-1") } returns listOf(work("w-1"))
        val vm = createViewModel()
        vm.loadWorks()

        coEvery { getWorksUseCase("h-1") } returns listOf(work("w-1"), work("w-2"))
        vm.refresh()

        val state = vm.uiState.value as WorksListUiState.Content
        assertEquals(2, state.works.size)
    }

    @Test
    fun `refresh - does nothing when state is not Content`() = runTest {
        val vm = createViewModel() // Loading state

        vm.refresh() // Should not crash or change state

        assertTrue(vm.uiState.value is WorksListUiState.Loading)
    }

    // endregion

    // region onWorkClick

    @Test
    fun `onWorkClick - sends NavigateToWorkDetail when in Content state`() = runTest {
        coEvery { getWorksUseCase("h-1") } returns listOf(work("w-1"))
        val vm = createViewModel(hiveId = "h-1")
        vm.loadWorks()

        vm.event.test {
            vm.onWorkClick("w-1")
            val event = awaitItem()
            assertTrue(event is WorksListEvent.NavigateToWorkDetail)
            assertEquals("w-1", (event as WorksListEvent.NavigateToWorkDetail).workId)
            assertEquals("h-1", event.hiveId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onWorkClick - does nothing when state is Loading`() = runTest {
        val vm = createViewModel()
        // State is Loading - no event should be sent
        vm.event.test {
            vm.onWorkClick("w-1")
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion

    // region onCreateClick

    @Test
    fun `onCreateClick - sends NavigateToWorkCreate when in Content state`() = runTest {
        coEvery { getWorksUseCase("h-1") } returns emptyList()
        val vm = createViewModel(hiveId = "h-1")
        vm.loadWorks()

        vm.event.test {
            vm.onCreateClick()
            val event = awaitItem()
            assertTrue(event is WorksListEvent.NavigateToWorkCreate)
            assertEquals("h-1", (event as WorksListEvent.NavigateToWorkCreate).hiveId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onCreateClick - does nothing when state is Error`() = runTest {
        coEvery { getWorksUseCase(any()) } throws RuntimeException()
        val vm = createViewModel()
        vm.loadWorks()

        vm.event.test {
            vm.onCreateClick()
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion

    // region resetError

    @Test
    fun `resetError - reloads works`() = runTest {
        coEvery { getWorksUseCase(any()) } throws RuntimeException()
        val vm = createViewModel()
        vm.loadWorks()
        assertTrue(vm.uiState.value is WorksListUiState.Error)

        coEvery { getWorksUseCase("h-1") } returns listOf(work("w-1"))
        vm.resetError()

        assertTrue(vm.uiState.value is WorksListUiState.Content)
    }

    // endregion

    private fun work(
        id: String = "w-1",
        title: String = "Work",
        hiveId: String = "h-1",
    ) = WorkDomain(
        id = id,
        hiveId = hiveId,
        title = title,
        text = "Some text",
        dateTime = LocalDateTime.of(2024, 7, 1, 8, 0)
    )
}
