package com.app.mobile.domain.usecase.hives.queen

import com.app.mobile.domain.mappers.toDomain
import com.app.mobile.domain.mappers.toRequest
import com.app.mobile.domain.models.hives.queen.QueenAddDomain
import com.app.mobile.domain.models.hives.queen.QueenCalendarRequestResult
import com.app.mobile.domain.repository.QueenLocalRepository
import com.app.mobile.domain.repository.RepositoryApi

// По-моему тут косяк мой
class SaveQueenUseCase(
    private val queenLocalRepository: QueenLocalRepository,
    private val repositoryApi: RepositoryApi
) {
    suspend operator fun invoke(queen: QueenAddDomain): Result<Unit> {
        return when (val apiResult = repositoryApi.calcQueenCalendar(queen.toRequest())) {
            is QueenCalendarRequestResult.Success -> {
                val fullQueenDomain = queen.toDomain(apiResult.queenLifecycle)
                queenLocalRepository.saveQueen(fullQueenDomain)

                Result.success(Unit)
            }

            is QueenCalendarRequestResult.Error -> {
                Result.failure(Exception(apiResult.error))
            }
        }
    }
}