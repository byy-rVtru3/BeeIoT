package com.app.mobile

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.app.mobile.presentation.navigation.getScreenFromIntent
import com.app.mobile.presentation.ui.screens.AppHost
import com.app.mobile.ui.theme.MobileTheme
import com.app.mobile.ui.theme.ThemeManager
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

	private val viewModel: MainViewModel by viewModel()
	private val themeManager: ThemeManager by inject()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		WindowCompat.setDecorFitsSystemWindows(window, false)

		val isDark = themeManager.themeState.value.isDarkTheme

		val bgColor = if (isDark) Color.parseColor("#121212") else Color.WHITE
		window.decorView.setBackgroundColor(bgColor)
		WindowInsetsControllerCompat(window, window.decorView).apply {
			isAppearanceLightStatusBars = !isDark
			isAppearanceLightNavigationBars = !isDark
		}

		val startRoute = viewModel.getRouteFromScreen(intent.getScreenFromIntent())

		setContent {
			val navController = rememberNavController()
			val themeState by themeManager.themeState.collectAsStateWithLifecycle()

			LaunchedEffect(Unit) {
				viewModel.navigationEvent.collect { route ->
					navController.navigate(route) {
						launchSingleTop = true
						restoreState = true
					}
				}
			}

			MobileTheme(darkTheme = themeState.isDarkTheme) {
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
