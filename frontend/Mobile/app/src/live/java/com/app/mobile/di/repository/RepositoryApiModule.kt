package com.app.mobile.di.repository

import com.app.mobile.data.converter.AuthorizationResponseConverter
import com.app.mobile.data.converter.CalcQueenCalendarConverter
import com.app.mobile.data.converter.ConfirmationResponseConverter
import com.app.mobile.data.converter.DeleteResponseConverter
import com.app.mobile.data.converter.LogoutResponseConverter
import com.app.mobile.data.converter.RegistrationResponseConverter
import com.app.mobile.data.repository.RepositoryApiImpl
import com.app.mobile.domain.repository.RepositoryApi
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val repositoryApiModule = module {

    factoryOf(::RegistrationResponseConverter)
    factoryOf(::ConfirmationResponseConverter)
    factoryOf(::AuthorizationResponseConverter)
    factoryOf(::LogoutResponseConverter)
    factoryOf(::DeleteResponseConverter)
    factoryOf(::CalcQueenCalendarConverter)

    factoryOf(::RepositoryApiImpl) bind RepositoryApi::class
}

