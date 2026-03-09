package com.app.mobile.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.app.mobile.data.session.manager.SessionManager
import com.app.mobile.domain.usecase.account.IsTokenExistUseCase
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(name = "session_manager")
val sessionModule = module {
	single<DataStore<Preferences>>(named("SessionStore")) {
		androidContext().sessionDataStore
	}

	single { SessionManager(dataStore = get(named("SessionStore"))) }

	factoryOf(::IsTokenExistUseCase)
}