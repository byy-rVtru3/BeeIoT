package com.app.mobile.di.screens

import com.app.mobile.domain.usecase.info.GetLocalInfoContentUseCase
import com.app.mobile.domain.usecase.info.SyncInfoContentUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val infoContentModule = module {
    factoryOf(::GetLocalInfoContentUseCase)
    factoryOf(::SyncInfoContentUseCase)
}
