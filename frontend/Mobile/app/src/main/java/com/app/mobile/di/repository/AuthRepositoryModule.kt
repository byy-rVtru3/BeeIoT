package com.app.mobile.di.repository

import com.app.mobile.data.repository.AuthRepository
import org.koin.dsl.module


val authRepository = module {
    single { AuthRepository(get(), get()) }
}