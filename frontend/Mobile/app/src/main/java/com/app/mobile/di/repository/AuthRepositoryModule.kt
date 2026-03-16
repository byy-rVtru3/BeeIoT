package com.app.mobile.di.repository

import com.app.mobile.data.repository.AuthRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module


val authRepository = module {
    singleOf(::AuthRepository)
}