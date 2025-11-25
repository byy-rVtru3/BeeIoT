package com.app.mobile.data.mock

import android.content.Context
import android.content.SharedPreferences

class MockDataSourceImpl(context: Context) : MockDataSource {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "mock_prefs",
        Context.MODE_PRIVATE
    )

    override fun isMock(): Boolean {
        return prefs.getBoolean(KEY_MOCK_ENABLED, true) // по умолчанию включен в develop
    }

    override fun setMock(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_MOCK_ENABLED, enabled).apply()
    }

    companion object {
        private const val KEY_MOCK_ENABLED = "mock_enabled"
    }
}

