package com.app.mobile.domain.usecase.hives.queen

import com.app.mobile.domain.repository.datasource.QueenDataSource
import java.time.LocalDate

class SaveQueenUseCase(private val queenDataSource: QueenDataSource) {

	suspend operator fun invoke(oldName: String?, newName: String, startDate: LocalDate) =
		if (oldName == null) queenDataSource.createQueen(newName, startDate)
		else queenDataSource.updateQueen(oldName, newName)
}
