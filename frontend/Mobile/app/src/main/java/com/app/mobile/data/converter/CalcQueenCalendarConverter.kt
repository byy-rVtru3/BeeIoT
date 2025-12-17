package com.app.mobile.data.converter

import com.app.mobile.data.api.mappers.toDomain
import com.app.mobile.data.api.models.queen.QueenCalendarResponse
import com.app.mobile.domain.models.hives.queen.QueenCalendarRequestResult
import retrofit2.Response


class CalcQueenCalendarConverter {
    fun convert(response: Response<QueenCalendarResponse>): QueenCalendarRequestResult {
        val body = response.body()
        val data = body?.data

        if (response.isSuccessful && data != null) {
            return QueenCalendarRequestResult.Success(data.toDomain())
        }
        val errorText = response.errorBody()?.string()
        val finalErrorMessage = if (!errorText.isNullOrBlank()) {
            errorText
        } else {
            response.message()
        }

        return QueenCalendarRequestResult.Error(finalErrorMessage)
    }
}