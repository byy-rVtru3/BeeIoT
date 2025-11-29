package com.app.mobile

import android.app.Application
import com.app.mobile.di.screens.authorizationModule
import com.app.mobile.di.screens.confirmationModule
import com.app.mobile.di.databaseModule
import com.app.mobile.di.networkModule
import com.app.mobile.di.publicApiModule
import com.app.mobile.di.repository.authRepository
import com.app.mobile.di.repository.hivesLocalRepositoryModule
import com.app.mobile.di.screens.registrationModule
import com.app.mobile.di.repository.repositoryApiModule
import com.app.mobile.di.repository.userLocalRepositoryModule
import com.app.mobile.di.screens.aboutAppModule
import com.app.mobile.di.screens.accountInfoModule
import com.app.mobile.di.screens.hivesListModule
import com.app.mobile.di.screens.settingsModule
import com.app.mobile.di.networkModules
import com.app.mobile.di.sessionModule
import com.app.mobile.di.repository.authRepository
import com.app.mobile.di.repository.repositoryApiModule
import com.app.mobile.di.repository.repositoryDatabaseModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MobileApp : Application() {
    // Suppress UNRESOLVED_REFERENCE because some references (e.g., BuildConfig) are generated at compile time
    // and may not be visible to the IDE during editing, leading to false unresolved reference warnings.
    @Suppress("UNRESOLVED_REFERENCE")
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@MobileApp)

            modules(
                registrationModule,
                repositoryApiModule,
                repositoryDatabaseModule,
                *networkModules.toTypedArray(),
                confirmationModule,
                authorizationModule,
                databaseModule,
                userLocalRepositoryModule,
                sessionModule,
                authRepository,
                settingsModule,
                aboutAppModule,
                accountInfoModule,
                hivesLocalRepositoryModule,
                hivesListModule
            )
        }
    }
}