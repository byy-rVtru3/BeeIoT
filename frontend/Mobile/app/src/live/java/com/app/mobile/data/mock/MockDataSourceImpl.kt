package com.app.mobile.data.mock

import android.content.Context

class MockDataSourceImpl(context: Context) : MockDataSource {

    override fun isMock(): Boolean = false

    override fun setMock(enabled: Boolean) {
        // Ничего не делаем в live версии
    }
}

