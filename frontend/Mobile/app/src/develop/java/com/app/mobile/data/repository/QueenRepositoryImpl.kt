package com.app.mobile.data.repository

import android.content.Context
import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.data.mock.MockDataProvider
import com.app.mobile.domain.models.hives.queen.QueenDomain
import com.app.mobile.domain.models.hives.queen.QueenDomainPreview
import com.app.mobile.domain.repository.QueenRepository
import kotlinx.coroutines.delay
import java.time.LocalDate

class QueenRepositoryImpl(private val context: Context) : QueenRepository {

    override suspend fun getQueens(): ApiResult<List<QueenDomainPreview>> {
        delay(100)
        return ApiResult.Success(
            listOf(
                QueenDomainPreview(name = "Матка-1", startDate = LocalDate.of(2026, 2, 19)),
                QueenDomainPreview(name = "Матка-2", startDate = LocalDate.of(2026, 3, 1))
            )
        )
    }

    override suspend fun getQueen(name: String): ApiResult<QueenDomain> {
        delay(100)
        val lifecycle = MockDataProvider.getQueenLifecycle(context)
        return ApiResult.Success(QueenDomain(name = name, stages = lifecycle))
    }

    override suspend fun createQueen(name: String, startDate: LocalDate): ApiResult<QueenDomain> {
        delay(100)
        val lifecycle = MockDataProvider.getQueenLifecycle(context)
        return ApiResult.Success(QueenDomain(name = name, stages = lifecycle))
    }

    override suspend fun updateQueen(oldName: String, newName: String?, startDate: String?): ApiResult<Unit> {
        delay(100)
        return ApiResult.Success(Unit)
    }

    override suspend fun deleteQueen(name: String): ApiResult<Unit> {
        delay(100)
        return ApiResult.Success(Unit)
    }
}
