package com.app.mobile.data.api.mappers

import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.data.api.models.HttpCode

fun ApiResult.HttpError.toDefaultMessage(): String = when (code) {
    HttpCode.BAD_REQUEST         -> "Некорректный запрос"
    HttpCode.UNAUTHORIZED        -> "Пользователь не авторизован"
    HttpCode.NOT_FOUND           -> "Не найдено"
    HttpCode.CONFLICT            -> "Конфликт данных"
    HttpCode.SERVER_ERROR        -> "Ошибка сервера"
    HttpCode.SERVICE_UNAVAILABLE -> "Сервис временно недоступен"
    HttpCode.GATEWAY_TIMEOUT     -> "Превышено время ожидания"
    else                         -> "Неизвестная ошибка (код $code)"
}

fun <T> ApiResult<T>.toErrorMessage(): String = when (this) {
    is ApiResult.Success         -> ""
    is ApiResult.HttpError       -> toDefaultMessage()
    is ApiResult.NetworkError    -> "Ошибка сети. Проверьте подключение к интернету"
    is ApiResult.UnexpectedError -> "Неизвестная ошибка"
}