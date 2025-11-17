package com.app.mobile.presentation.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.app.mobile.presentation.navigation.AppNavigation

@Composable
fun AppHost() {
    val navController = rememberNavController()

    Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
        AppNavigation(
            modifier = Modifier.padding(paddingValues),
            navController = navController
        )
    }
}