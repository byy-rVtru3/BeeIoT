package com.app.mobile

import android.app.Application
import com.app.mobile.di.authorizationModule
import com.app.mobile.di.confirmationModule
import com.app.mobile.di.networkModule
import com.app.mobile.di.registrationModule
import com.app.mobile.di.repositoryModule
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
                repositoryModule,
                networkModule,
                confirmationModule,
                authorizationModule
            )
        }
    }
}