package com.app.mobile.data.mock

interface MockDataSource {
    fun isMock(): Boolean
    fun setMock(enabled: Boolean)
}

