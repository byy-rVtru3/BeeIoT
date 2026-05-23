package com.app.mobile.domain.usecase.works

import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.domain.models.hives.WorkDomain
import com.app.mobile.domain.repository.WorkRepository
import com.app.mobile.domain.usecase.hives.works.AddWorkUseCase
import com.app.mobile.domain.usecase.hives.works.CreateWorkUseCase
import com.app.mobile.domain.usecase.hives.works.DeleteWorkUseCase
import com.app.mobile.domain.usecase.hives.works.GetWorkUseCase
import com.app.mobile.domain.usecase.hives.works.GetWorksUseCase
import com.app.mobile.domain.usecase.hives.works.UpdateWorkUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime

class WorksUseCasesTest {

    private val repo = mockk<WorkRepository>()

    // region GetWorksUseCase

    @Test
    fun `GetWorksUseCase - delegates to repo with correct hiveName`() = runTest {
        val works = listOf(work(id = "1"), work(id = "2"))
        coEvery { repo.getWorks("hive-1") } returns ApiResult.Success(works)

        val result = GetWorksUseCase(repo).invoke("hive-1")

        assertEquals(ApiResult.Success(works), result)
        coVerify(exactly = 1) { repo.getWorks("hive-1") }
    }

    // endregion

    // region GetWorkUseCase

    @Test
    fun `GetWorkUseCase - returns work when found`() = runTest {
        val expected = work(id = "w-1")
        coEvery { repo.getWork("w-1") } returns ApiResult.Success(expected)

        val result = GetWorkUseCase(repo).invoke("w-1")

        assertEquals(ApiResult.Success(expected), result)
    }

    // endregion

    // region DeleteWorkUseCase

    @Test
    fun `DeleteWorkUseCase - delegates to repo with correct workId`() = runTest {
        coEvery { repo.deleteWork("w-1") } returns ApiResult.Success(Unit)

        DeleteWorkUseCase(repo).invoke("w-1")

        coVerify(exactly = 1) { repo.deleteWork("w-1") }
    }

    // endregion

    // region AddWorkUseCase

    @Test
    fun `AddWorkUseCase - delegates to repo with correct work`() = runTest {
        val newWork = work(id = "w-2")
        coEvery { repo.addWork(newWork) } returns ApiResult.Success(Unit)

        AddWorkUseCase(repo).invoke(newWork)

        coVerify(exactly = 1) { repo.addWork(newWork) }
    }

    // endregion

    // region UpdateWorkUseCase

    @Test
    fun `UpdateWorkUseCase - delegates to repo with correct work`() = runTest {
        val updated = work(id = "w-3")
        coEvery { repo.updateWork(updated) } returns ApiResult.Success(Unit)

        UpdateWorkUseCase(repo).invoke(updated)

        coVerify(exactly = 1) { repo.updateWork(updated) }
    }

    // endregion

    // region CreateWorkUseCase

    @Test
    fun `CreateWorkUseCase - creates work with correct hiveId and empty fields`() = runTest {
        val result = CreateWorkUseCase().invoke("hive-1")

        assertEquals("hive-1", result.hiveId)
        assertEquals("", result.title)
        assertEquals("", result.text)
    }

    @Test
    fun `CreateWorkUseCase - creates work with unique id`() = runTest {
        val useCase = CreateWorkUseCase()
        val r1 = useCase.invoke("h1")
        val r2 = useCase.invoke("h1")

        assert(r1.id != r2.id) { "Created works should have unique IDs" }
    }

    // endregion

    private fun work(
        id: String = "id",
        hiveId: String = "hive-1",
        title: String = "Title",
        text: String = "Text",
    ) = WorkDomain(
        id = id,
        hiveId = hiveId,
        title = title,
        text = text,
        dateTime = LocalDateTime.of(2024, 6, 1, 12, 0)
    )
}
