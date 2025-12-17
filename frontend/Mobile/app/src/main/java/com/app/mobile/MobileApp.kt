package com.app.mobile

import android.app.Application
import com.app.mobile.di.databaseModule
import com.app.mobile.di.jsonModule
import com.app.mobile.di.networkModules
import com.app.mobile.di.repository.authRepository
import com.app.mobile.di.repository.hivesLocalRepositoryModule
import com.app.mobile.di.repository.hubLocalRepositoryModule
import com.app.mobile.di.repository.queenLocalRepositoryModule
import com.app.mobile.di.repository.repositoryApiModule
import com.app.mobile.di.repository.repositoryDatabaseModule
import com.app.mobile.di.repository.userLocalRepositoryModule
import com.app.mobile.di.screens.*
import com.app.mobile.di.sessionModule
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
                jsonModule,
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
                queenLocalRepositoryModule,
                hubLocalRepositoryModule,
                hivesListModule,
                hiveModule,
                hiveEditorModule,
                queenModule,
                queenEditorModule,
                queenListModule
            )
        }
    }
}