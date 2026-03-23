package com.app.mobile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.mobile.domain.usecase.account.IsTokenExistUseCase
import com.app.mobile.presentation.navigation.Screen
import com.app.mobile.presentation.ui.screens.authorization.AuthorizationRoute
import com.app.mobile.presentation.ui.screens.hive.list.HivesListRoute
import com.app.mobile.presentation.ui.screens.main.MainRoute
import com.app.mobile.presentation.ui.screens.settings.SettingsRoute
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class MainViewModel(
	private val isTokenExistUseCase: IsTokenExistUseCase
) : ViewModel() {

	private val _navigationEvent = Channel<Any>()
	val navigationEvent = _navigationEvent.receiveAsFlow()

	fun getRouteFromScreen(screen: Screen?): Any {

		val session = isTokenExistUseCase()

		return if (!session) {
			AuthorizationRoute
		} else {
			when (screen) {
				Screen.SCREEN_HIVES -> HivesListRoute
				Screen.SCREEN_MAIN  -> MainRoute
				else                -> SettingsRoute
			}
		}
	}

	fun handleHotStartIntent(screen: Screen?) {
		val route = getRouteFromScreen(screen)
		viewModelScope.launch {
			_navigationEvent.send(route)
		}
	}
}