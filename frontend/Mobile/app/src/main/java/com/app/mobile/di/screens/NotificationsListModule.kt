package com.app.mobile.di.screens

import com.app.mobile.presentation.ui.screens.notifications.viewmodel.NotificationsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val notificationsListModule = module {
    viewModelOf(::NotificationsViewModel)
}
