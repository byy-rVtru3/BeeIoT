package com.app.mobile

import android.app.Application
import com.app.mobile.di.databaseModule
import com.app.mobile.di.jsonModule
import com.app.mobile.di.mainModule
import com.app.mobile.di.networkModules
import com.app.mobile.di.firebaseModule
import com.app.mobile.di.notificationsModule
import com.app.mobile.di.themeModule
import com.app.mobile.di.repository.authRepository
import com.app.mobile.di.repository.hivesRepositoryModule
import com.app.mobile.di.repository.hubRepositoryModule
import com.app.mobile.di.repository.queenRepositoryModule
import com.app.mobile.di.repository.repositoryApiModule
import com.app.mobile.di.repository.repositoryDatabaseModule
import com.app.mobile.di.repository.userLocalRepositoryModule
import com.app.mobile.di.repository.workLocalRepositoryModule
import com.app.mobile.di.screens.aboutAppModule
import com.app.mobile.di.screens.accountInfoModule
import com.app.mobile.di.screens.authorizationModule
import com.app.mobile.di.screens.confirmationModule
import com.app.mobile.di.screens.hubsListModule
import com.app.mobile.di.screens.hubModule
import com.app.mobile.di.screens.hubEditorModule
import com.app.mobile.di.screens.hiveEditorModule
import com.app.mobile.di.screens.hiveModule
import com.app.mobile.di.screens.hivesListModule
import com.app.mobile.di.screens.queenEditorModule
import com.app.mobile.di.screens.queenListModule
import com.app.mobile.di.screens.queenModule
import com.app.mobile.di.screens.registrationModule
import com.app.mobile.di.screens.settingsModule
import com.app.mobile.di.screens.workEditorModule
import com.app.mobile.di.screens.workListModule
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
				hivesRepositoryModule,
				queenRepositoryModule,
				hubRepositoryModule,
				hivesListModule,
				hiveModule,
				hiveEditorModule,
				hubsListModule,
				hubModule,
				hubEditorModule,
				queenModule,
				queenEditorModule,
				queenListModule,
				workLocalRepositoryModule,
				workEditorModule,
				workListModule,
				notificationsModule,
				themeModule,
				mainModule,
				firebaseModule
			)
		}
	}
}
