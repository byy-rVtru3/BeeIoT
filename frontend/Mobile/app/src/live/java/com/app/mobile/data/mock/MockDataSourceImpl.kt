package com.app.mobile.data.mock

import android.content.Context

class MockDataSourceImpl(context: Context) {

    fun isMock(): Boolean = false

    fun setMock(enabled: Boolean) {
        // Ничего не делаем в live версии
    }

    fun isValidationEnabled(): Boolean = true  // Всегда включена в live

    fun setValidationEnabled(enabled: Boolean) {
        // Ничего не делаем в live версии
    }
}
