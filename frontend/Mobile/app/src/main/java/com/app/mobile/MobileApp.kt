package com.app.mobile

import android.app.Application
import com.app.mobile.di.authApiModule
import com.app.mobile.di.screens.authorizationModule
import com.app.mobile.di.screens.confirmationModule
import com.app.mobile.di.databaseModule
import com.app.mobile.di.networkModule
import com.app.mobile.di.publicApiModule
import com.app.mobile.di.repository.authRepository
import com.app.mobile.di.screens.registrationModule
import com.app.mobile.di.repository.repositoryApiModule
import com.app.mobile.di.repository.repositoryDatabaseModule
import com.app.mobile.di.screens.settingsModule
import com.app.mobile.di.sessionModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MobileApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@MobileApp)

            modules(
                registrationModule,
                repositoryApiModule,
                networkModule,
                confirmationModule,
                authorizationModule,
                databaseModule,
                repositoryDatabaseModule,
                sessionModule,
                publicApiModule,
                authApiModule,
                authRepository,
                settingsModule
            )
        }
    }
}