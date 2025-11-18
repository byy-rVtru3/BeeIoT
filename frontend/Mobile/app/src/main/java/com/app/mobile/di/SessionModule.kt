package com.app.mobile.di

import com.app.mobile.data.session.manager.SessionManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val sessionModule = module {
    single { SessionManager(androidContext()) }
}