package com.app.mobile.di.repository

import com.app.mobile.data.content.InfoContentCacheStore
import com.app.mobile.data.content.InfoContentDefaultsProvider
import com.app.mobile.data.repository.InfoContentRepositoryImpl
import com.app.mobile.domain.repository.InfoContentRepository
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

val infoContentRepositoryModule = module {
    singleOf(::InfoContentDefaultsProvider)

    single {
        InfoContentCacheStore(
            dataStore = get(named("SettingsStore")),
            json = get()
        )
    }

    singleOf(::InfoContentRepositoryImpl) bind InfoContentRepository::class
}
