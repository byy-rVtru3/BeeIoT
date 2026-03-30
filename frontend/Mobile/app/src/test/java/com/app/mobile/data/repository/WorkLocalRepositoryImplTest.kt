package com.app.mobile.data.repository

import com.app.mobile.data.datastore.dao.WorkDao
import com.app.mobile.data.datastore.entity.WorkEntity
import com.app.mobile.domain.models.hives.WorkDomain
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime

class WorkLocalRepositoryImplTest {

    private val workDao = mockk<WorkDao>()
    private val repository = WorkLocalRepositoryImpl(workDao)

    private val fixedDateTime = LocalDateTime.of(2024, 5, 10, 14, 30)

    // region getWork

    @Test
    fun `getWork - returns mapped WorkDomain when entity found`() = runTest {
        val entity = workEntity(id = "w-1")
        coEvery { workDao.getWork("w-1") } returns entity

        val result = repository.getWork("w-1")

        assertEquals("w-1", result?.id)
        assertEquals("hive-1", result?.hiveId)
        assertEquals("Test Title", result?.title)
        assertEquals("Test Text", result?.text)
        assertEquals(fixedDateTime, result?.dateTime)
    }

    @Test
    fun `getWork - returns null when entity not found`() = runTest {
        coEvery { workDao.getWork("missing") } returns null

        val result = repository.getWork("missing")

        assertNull(result)
    }

    // endregion

    // region getWorks

    @Test
    fun `getWorks - returns mapped list of WorkDomain`() = runTest {
        val entities = listOf(workEntity(id = "w-1"), workEntity(id = "w-2"))
        coEvery { workDao.getWorks("hive-1") } returns entities

        val result = repository.getWorks("hive-1")

        assertEquals(2, result.size)
        assertEquals("w-1", result[0].id)
        assertEquals("w-2", result[1].id)
    }

    @Test
    fun `getWorks - returns empty list when no works found`() = runTest {
        coEvery { workDao.getWorks("hive-empty") } returns emptyList()

        val result = repository.getWorks("hive-empty")

        assertEquals(emptyList<WorkDomain>(), result)
    }

    // endregion

    // region saveWork

    @Test
    fun `saveWork - delegates to dao with mapped entity`() = runTest {
        val work = workDomain(id = "w-3")
        coEvery { workDao.saveWork(any()) } returns Unit

        repository.saveWork(work)

        coVerify(exactly = 1) {
            workDao.saveWork(match { it.id == "w-3" && it.hiveId == "hive-1" })
        }
    }

    // endregion

    // region deleteWork

    @Test
    fun `deleteWork - delegates to dao with correct workId`() = runTest {
        coEvery { workDao.deleteWork("w-4") } returns Unit

        repository.deleteWork("w-4")

        coVerify(exactly = 1) { workDao.deleteWork("w-4") }
    }

    // endregion

    private fun workEntity(
        id: String = "w-1",
        hiveId: String = "hive-1",
        title: String = "Test Title",
        text: String = "Test Text",
    ) = WorkEntity(id = id, hiveId = hiveId, title = title, text = text, dateTime = fixedDateTime)

    private fun workDomain(
        id: String = "w-1",
        hiveId: String = "hive-1",
        title: String = "Test Title",
        text: String = "Test Text",
    ) = WorkDomain(id = id, hiveId = hiveId, title = title, text = text, dateTime = fixedDateTime)
}
