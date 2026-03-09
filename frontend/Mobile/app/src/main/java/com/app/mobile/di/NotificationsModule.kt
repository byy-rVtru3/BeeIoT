package com.app.mobile.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.app.mobile.data.datastore.DeviceIdDataSource
import com.app.mobile.data.datastore.DeviceIdDataSourceImpl
import com.app.mobile.data.datastore.FcmTokenDataSource
import com.app.mobile.data.datastore.FcmTokenDataSourceImpl
import com.app.mobile.data.repository.notifications.DeviceIdRepositoryImpl
import com.app.mobile.data.repository.notifications.PermissionRepositoryImpl
import com.app.mobile.data.repository.notifications.PushTokenRepositoryImpl
import com.app.mobile.domain.repository.notifications.DeviceIdRepository
import com.app.mobile.domain.repository.notifications.PermissionRepository
import com.app.mobile.domain.repository.notifications.PushTokenRepository
import com.app.mobile.domain.repository.notifications.TokenRetryScheduler
import com.app.mobile.domain.scenario.RegisterPushTokenScenario
import com.app.mobile.domain.usecase.notifications.CheckIfNotificationPromptShownUseCase
import com.app.mobile.domain.usecase.notifications.SendPushTokenUseCase
import com.app.mobile.domain.usecase.notifications.SetNotificationPromptShownUseCase
import com.app.mobile.presentation.notifications.NotificationChannelsInitializer
import com.app.mobile.presentation.notifications.NotificationChannelsInitializerImpl
import com.app.mobile.presentation.notifications.NotificationView
import com.app.mobile.presentation.notifications.NotificationViewImpl
import com.app.mobile.presentation.notifications.PushController
import com.app.mobile.worker.FcmTokenRetryScheduler
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings_manager")
val notificationsModule = module {

	single<DataStore<Preferences>>(named("SettingsStore")) {
		androidContext().settingsDataStore
	}

	factoryOf(::FcmTokenDataSourceImpl) bind FcmTokenDataSource::class

	factoryOf(::PushTokenRepositoryImpl) bind PushTokenRepository::class

	factoryOf(::DeviceIdDataSourceImpl) bind DeviceIdDataSource::class

	factoryOf(::DeviceIdRepositoryImpl) bind DeviceIdRepository::class

	factoryOf(::SendPushTokenUseCase)

	factoryOf(::RegisterPushTokenScenario)

	factoryOf(::FcmTokenRetryScheduler) bind TokenRetryScheduler::class

	singleOf(::NotificationChannelsInitializerImpl) bind NotificationChannelsInitializer::class

	factoryOf(::NotificationViewImpl) bind NotificationView::class

	single { PermissionRepositoryImpl(
		dataStore = get(named("SettingsStore"))
	) } bind PermissionRepository::class

	factory {
		PushController(
			dispatcher = Dispatchers.Main,
			registerPushTokenScenario = get(),
			notificationView = get()
		)
	}

	factoryOf(::SetNotificationPromptShownUseCase)

	factoryOf(::CheckIfNotificationPromptShownUseCase)
}