package com.app.mobile.domain.repository.datasource

import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.domain.models.hives.queen.QueenDomain
import com.app.mobile.domain.models.hives.queen.QueenDomainPreview
import java.time.LocalDate

interface QueenDataSource {
    suspend fun getQueens(): ApiResult<List<QueenDomainPreview>>
    suspend fun getQueen(name: String): ApiResult<QueenDomain>
    suspend fun createQueen(name: String, startDate: LocalDate): ApiResult<QueenDomain>
    suspend fun updateQueen(oldName: String, newName: String? = null, startDate: String? = null): ApiResult<Unit>
    suspend fun deleteQueen(name: String): ApiResult<Unit>
    suspend fun getQueensWithCalendars(): ApiResult<List<QueenDomain>>
}
