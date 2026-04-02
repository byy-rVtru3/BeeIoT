package com.app.mobile.data.repository

import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.domain.models.hives.WorkDomain
import com.app.mobile.domain.repository.WorkRepository
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.util.UUID

class WorkRepositoryImpl : WorkRepository {

    private val mockWorks = mutableListOf(
        WorkDomain(
            id = "work-1",
            hiveId = "Улей-1",
            title = "Осенняя ревизия",
            text = "Проверка наличия кормов, сокращение гнезда, обработка от клеща",
            dateTime = LocalDateTime.now().minusDays(5)
        ),
        WorkDomain(
            id = "work-2",
            hiveId = "Улей-1",
            title = "Обработка от клеща",
            text = "Препарат Бипин, 2 полоски",
            dateTime = LocalDateTime.now().minusDays(2)
        )
    )

    override suspend fun getWorks(hiveName: String): ApiResult<List<WorkDomain>> {
        delay(100)
        return ApiResult.Success(mockWorks.filter { it.hiveId == hiveName })
    }

    override suspend fun getWork(workId: String): ApiResult<WorkDomain?> {
        delay(100)
        return ApiResult.Success(mockWorks.find { it.id == workId })
    }

    override suspend fun addWork(work: WorkDomain): ApiResult<Unit> {
        delay(100)
        mockWorks.add(work.copy(id = UUID.randomUUID().toString(), dateTime = LocalDateTime.now()))
        return ApiResult.Success(Unit)
    }

    override suspend fun updateWork(work: WorkDomain): ApiResult<Unit> {
        delay(100)
        val index = mockWorks.indexOfFirst { it.id == work.id }
        if (index != -1) mockWorks[index] = work
        return ApiResult.Success(Unit)
    }

    override suspend fun deleteWork(workId: String): ApiResult<Unit> {
        delay(100)
        mockWorks.removeAll { it.id == workId }
        return ApiResult.Success(Unit)
    }
}
