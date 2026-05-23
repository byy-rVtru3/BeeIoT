package com.app.mobile.domain.usecase.hives.queen

import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.domain.repository.datasource.QueenDataSource

class DeleteQueenUseCase(private val queenDataSource: QueenDataSource) {
    suspend operator fun invoke(name: String): ApiResult<Unit> =
        queenDataSource.deleteQueen(name)
}
