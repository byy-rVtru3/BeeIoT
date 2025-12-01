package com.app.mobile.data.mock

import android.content.Context
import android.content.SharedPreferences
import com.app.mobile.presentation.validators.ValidationStateProvider

class MockDataSourceImpl(context: Context) : ValidationStateProvider {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "mock_prefs",
        Context.MODE_PRIVATE
    )

    fun isMock(): Boolean {
        return prefs.getBoolean(KEY_MOCK_ENABLED, true) // по умолчанию включен в develop
    }

    fun setMock(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_MOCK_ENABLED, enabled).apply()
    }

    override fun isValidationEnabled(): Boolean {
        return prefs.getBoolean(KEY_VALIDATION_ENABLED, true) // по умолчанию включена
    }

    override fun setValidationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_VALIDATION_ENABLED, enabled).apply()
    }

    companion object {
        private const val KEY_MOCK_ENABLED = "mock_enabled"
        private const val KEY_VALIDATION_ENABLED = "validation_enabled"
    }
}
