package com.app.mobile

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.app.mobile.presentation.navigation.getScreenFromIntent
import com.app.mobile.presentation.ui.screens.AppHost
import com.app.mobile.presentation.ui.screens.authorization.AuthorizationRoute
import com.app.mobile.ui.theme.MobileTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

	private val viewModel: MainViewModel by viewModel()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()

		WindowCompat.setDecorFitsSystemWindows(window, false)

		val startRoute = viewModel.getRouteFromScreen(intent.getScreenFromIntent())

		setContent {
			val navController = rememberNavController()

			LaunchedEffect(Unit) {
				viewModel.navigationEvent.collect { route ->
					navController.navigate(route) {
						launchSingleTop = true
						restoreState = true
					}
				}
			}
			MobileTheme {
				AppHost(
					startRoute,
					navController
				)
			}
		}
	}

	override fun onNewIntent(intent: Intent) {
		super.onNewIntent(intent)
		viewModel.handleHotStartIntent(intent.getScreenFromIntent())
	}
}
