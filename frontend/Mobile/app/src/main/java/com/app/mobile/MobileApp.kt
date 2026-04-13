package com.app.mobile

import android.app.Application
import com.app.mobile.di.jsonModule
import com.app.mobile.di.mainModule
import com.app.mobile.di.networkModules
import com.app.mobile.di.firebaseModule
import com.app.mobile.di.notificationsModule
import com.app.mobile.di.themeModule
import com.app.mobile.di.repository.authRepository
import com.app.mobile.di.repository.hivesDataSourceModule
import com.app.mobile.di.repository.hubRepositoryModule
import com.app.mobile.di.repository.queenDataSourceModule
import com.app.mobile.di.repository.repositoryApiModule
import com.app.mobile.di.repository.telemetryRepositoryModule
import com.app.mobile.di.repository.workRepositoryModule
import com.app.mobile.di.screens.aboutAppModule
import com.app.mobile.di.screens.homeModule
import com.app.mobile.di.screens.accountInfoModule
import com.app.mobile.di.screens.authorizationModule
import com.app.mobile.di.screens.confirmationModule
import com.app.mobile.di.screens.hubsListModule
import com.app.mobile.di.screens.hubModule
import com.app.mobile.di.screens.hubEditorModule
import com.app.mobile.di.screens.hiveEditorModule
import com.app.mobile.di.screens.hiveModule
import com.app.mobile.di.screens.hivesListModule
import com.app.mobile.di.screens.howToUseModule
import com.app.mobile.di.screens.queenEditorModule
import com.app.mobile.di.screens.queenListModule
import com.app.mobile.di.screens.queenModule
import com.app.mobile.di.screens.registrationModule
import com.app.mobile.di.screens.sensorChartModule
import com.app.mobile.di.screens.settingsModule
import com.app.mobile.di.screens.workDetailModule
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
				*networkModules.toTypedArray(),
				confirmationModule,
				authorizationModule,
				sessionModule,
				authRepository,
				settingsModule,
				aboutAppModule,
				howToUseModule,
				accountInfoModule,
				hivesDataSourceModule,
				queenDataSourceModule,
				hubRepositoryModule,
				telemetryRepositoryModule,
				hivesListModule,
				hiveModule,
				hiveEditorModule,
				hubsListModule,
				hubModule,
				hubEditorModule,
				queenModule,
				queenEditorModule,
				queenListModule,
				workRepositoryModule,
				workDetailModule,
				sensorChartModule,
				workEditorModule,
				workListModule,
				homeModule,
				notificationsModule,
				themeModule,
				mainModule,
				firebaseModule
			)
		}
	}
}
