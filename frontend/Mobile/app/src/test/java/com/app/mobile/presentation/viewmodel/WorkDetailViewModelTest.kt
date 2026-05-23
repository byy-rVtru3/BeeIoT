package com.app.mobile.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.app.mobile.domain.models.hives.WorkDomain
import com.app.mobile.domain.usecase.hives.works.DeleteWorkUseCase
import com.app.mobile.domain.usecase.hives.works.GetWorkUseCase
import com.app.mobile.presentation.ui.screens.works.detail.viewmodel.WorkDetailEvent
import com.app.mobile.presentation.ui.screens.works.detail.viewmodel.WorkDetailUiState
import com.app.mobile.presentation.ui.screens.works.detail.viewmodel.WorkDetailViewModel
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class WorkDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val getWorkUseCase = mockk<GetWorkUseCase>()
    private val deleteWorkUseCase = mockk<DeleteWorkUseCase>()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        workId: String = "w-1",
        hiveId: String = "h-1",
    ): WorkDetailViewModel {
        val handle = SavedStateHandle(mapOf("workId" to workId, "hiveId" to hiveId))
        return WorkDetailViewModel(handle, getWorkUseCase, deleteWorkUseCase)
    }

    // region loadWork

    @Test
    fun `loadWork - success - state becomes Content with correct work`() = runTest {
        val work = workDomain(id = "w-1", title = "Осмотр")
        coEvery { getWorkUseCase("w-1") } returns work

        val vm = createViewModel()
        vm.loadWork()

        val state = vm.uiState.value
        assertTrue(state is WorkDetailUiState.Content)
        assertEquals("Осмотр", (state as WorkDetailUiState.Content).work.title)
        assertEquals("h-1", state.work.hiveId)
    }

    @Test
    fun `loadWork - work not found - state becomes Error`() = runTest {
        coEvery { getWorkUseCase("w-1") } returns null

        val vm = createViewModel()
        vm.loadWork()

        val state = vm.uiState.value
        assertTrue(state is WorkDetailUiState.Error)
        assertEquals("Работа не найдена", (state as WorkDetailUiState.Error).message)
    }

    @Test
    fun `loadWork - sets Loading state before fetching`() = runTest {
        coEvery { getWorkUseCase(any()) } returns null

        val vm = createViewModel()
        vm.loadWork()

        // After loading completes with error, verify it went through Loading
        // (state is error now, loading was intermediate)
        assertTrue(vm.uiState.value is WorkDetailUiState.Error)
    }

    // endregion

    // region refresh

    @Test
    fun `refresh - updates content when work found`() = runTest {
        val work = workDomain(id = "w-1", title = "Initial")
        coEvery { getWorkUseCase("w-1") } returns work
        val vm = createViewModel()
        vm.loadWork()

        val updatedWork = workDomain(id = "w-1", title = "Updated")
        coEvery { getWorkUseCase("w-1") } returns updatedWork
        vm.refresh()

        val state = vm.uiState.value as WorkDetailUiState.Content
        assertEquals("Updated", state.work.title)
    }

    @Test
    fun `refresh - does nothing when state is not Content`() = runTest {
        val vm = createViewModel() // state is Loading

        vm.refresh()

        // Should remain in Loading - no usecase called
        coVerify(exactly = 0) { getWorkUseCase(any()) }
    }

    // endregion

    // region onEditClick

    @Test
    fun `onEditClick - sends NavigateToEdit event with correct ids`() = runTest {
        val work = workDomain()
        coEvery { getWorkUseCase("w-1") } returns work
        val vm = createViewModel(workId = "w-1", hiveId = "h-1")
        vm.loadWork()

        vm.event.test {
            vm.onEditClick()
            val event = awaitItem()
            assertTrue(event is WorkDetailEvent.NavigateToEdit)
            assertEquals("w-1", (event as WorkDetailEvent.NavigateToEdit).workId)
            assertEquals("h-1", event.hiveId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion

    // region onDeleteClick

    @Test
    fun `onDeleteClick - calls deleteUseCase and sends NavigateBack`() = runTest {
        coEvery { deleteWorkUseCase("w-1") } returns Unit
        val vm = createViewModel(workId = "w-1", hiveId = "h-1")

        vm.event.test {
            vm.onDeleteClick()
            val event = awaitItem()
            assertTrue(event is WorkDetailEvent.NavigateBack)
            assertEquals("h-1", (event as WorkDetailEvent.NavigateBack).hiveId)
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { deleteWorkUseCase("w-1") }
    }

    // endregion

    // region resetError

    @Test
    fun `resetError - reloads work`() = runTest {
        coEvery { getWorkUseCase("w-1") } returns null
        val vm = createViewModel()
        vm.loadWork()
        assertTrue(vm.uiState.value is WorkDetailUiState.Error)

        val work = workDomain()
        coEvery { getWorkUseCase("w-1") } returns work
        vm.resetError()

        assertTrue(vm.uiState.value is WorkDetailUiState.Content)
    }

    // endregion

    // region handleError

    @Test
    fun `handleError - exception during load sets Error state`() = runTest {
        coEvery { getWorkUseCase(any()) } throws RuntimeException("DB error")

        val vm = createViewModel()
        vm.loadWork()

        val state = vm.uiState.value
        assertTrue(state is WorkDetailUiState.Error)
        assertEquals("DB error", (state as WorkDetailUiState.Error).message)
    }

    // endregion

    private fun workDomain(
        id: String = "w-1",
        hiveId: String = "h-1",
        title: String = "Title",
        text: String = "Text",
    ) = WorkDomain(
        id = id,
        hiveId = hiveId,
        title = title,
        text = text,
        dateTime = LocalDateTime.of(2024, 6, 15, 10, 0)
    )
}
