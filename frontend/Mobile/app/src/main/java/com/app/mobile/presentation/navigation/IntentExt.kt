package com.app.mobile.presentation.navigation

import android.content.Intent

private const val TARGET_SCREEN = "TARGET_SCREEN"

fun Intent.putScreenInIntent(screen: Screen): Intent {
	return this.putExtra(TARGET_SCREEN, screen.ordinal)
}

fun Intent.getScreenFromIntent(): Screen? {
	val screenId = this.getIntExtra(TARGET_SCREEN, -1)
	return if (screenId != -1) {
		Screen.entries[this.getIntExtra(TARGET_SCREEN, 0)]
	} else {
		null
	}
}