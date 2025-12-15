package com.app.mobile.di.repository

import com.app.mobile.data.repository.HubLocalRepositorImpl
import com.app.mobile.domain.repository.HubLocalRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module


val hubLocalRepositoryModule = module {

    singleOf(::HubLocalRepositorImpl) bind HubLocalRepository::class

}