package com.app.mobile.domain.usecase.works

import com.app.mobile.domain.models.hives.WorkDomain
import com.app.mobile.domain.repository.WorkLocalRepository
import com.app.mobile.domain.usecase.hives.works.CreateWorkUseCase
import com.app.mobile.domain.usecase.hives.works.DeleteWorkUseCase
import com.app.mobile.domain.usecase.hives.works.GetWorkUseCase
import com.app.mobile.domain.usecase.hives.works.GetWorksUseCase
import com.app.mobile.domain.usecase.hives.works.SaveWorkUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime

class WorksUseCasesTest {

    private val repo = mockk<WorkLocalRepository>()

    // region GetWorksUseCase

    @Test
    fun `GetWorksUseCase - delegates to repo with correct hiveId`() = runTest {
        val works = listOf(work(id = "1"), work(id = "2"))
        coEvery { repo.getWorks("hive-1") } returns works

        val result = GetWorksUseCase(repo).invoke("hive-1")

        assertEquals(works, result)
        coVerify(exactly = 1) { repo.getWorks("hive-1") }
    }

    @Test
    fun `GetWorksUseCase - returns empty list when no works`() = runTest {
        coEvery { repo.getWorks(any()) } returns emptyList()

        val result = GetWorksUseCase(repo).invoke("hive-empty")

        assertEquals(emptyList<WorkDomain>(), result)
    }

    // endregion

    // region GetWorkUseCase

    @Test
    fun `GetWorkUseCase - returns work when found`() = runTest {
        val expected = work(id = "w-1")
        coEvery { repo.getWork("w-1") } returns expected

        val result = GetWorkUseCase(repo).invoke("w-1")

        assertEquals(expected, result)
    }

    @Test
    fun `GetWorkUseCase - returns null when not found`() = runTest {
        coEvery { repo.getWork("missing") } returns null

        val result = GetWorkUseCase(repo).invoke("missing")

        assertNull(result)
    }

    // endregion

    // region DeleteWorkUseCase

    @Test
    fun `DeleteWorkUseCase - delegates to repo with correct workId`() = runTest {
        coEvery { repo.deleteWork("w-1") } returns Unit

        DeleteWorkUseCase(repo).invoke("w-1")

        coVerify(exactly = 1) { repo.deleteWork("w-1") }
    }

    // endregion

    // region SaveWorkUseCase

    @Test
    fun `SaveWorkUseCase - delegates to repo with correct work`() = runTest {
        val work = work(id = "w-2")
        coEvery { repo.saveWork(work) } returns Unit

        SaveWorkUseCase(repo).invoke(work)

        coVerify(exactly = 1) { repo.saveWork(work) }
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
