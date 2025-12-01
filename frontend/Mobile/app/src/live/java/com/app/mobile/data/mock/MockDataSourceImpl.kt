package com.app.mobile.data.mock

import android.content.Context
import com.app.mobile.presentation.validators.ValidationStateProvider

class MockDataSourceImpl(context: Context) : ValidationStateProvider {

    fun isMock(): Boolean = false

    fun setMock(enabled: Boolean) {
        // Ничего не делаем в live версии
    }

    override fun isValidationEnabled(): Boolean = true

    override fun setValidationEnabled(enabled: Boolean) {
        // Ничего не делаем в live версии
    }
}
