package com.app.mobile.domain.usecase.hives.queen

import com.app.mobile.domain.mappers.toRequest
import com.app.mobile.domain.models.hives.queen.QueenEditorDomain
import com.app.mobile.domain.repository.RepositoryApi

class CalcQueenCalendarUseCase(private val repositoryApi: RepositoryApi) {
    suspend operator fun invoke(queen: QueenEditorDomain) =
        repositoryApi.calcQueenCalendar(queen.toRequest())
}