package com.app.mobile.presentation.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.app.mobile.presentation.navigation.AppNavigation
import com.app.mobile.presentation.ui.components.AppBottomBar
import com.app.mobile.presentation.ui.screens.hive.list.HivesListRoute
import com.app.mobile.presentation.ui.screens.settings.SettingsRoute


//@Composable
//fun AppHost() {
//    val navController = rememberNavController()
//    val navBackStackEntry by navController.currentBackStackEntryAsState()
//    val currentDestination = navBackStackEntry?.destination
//
//
//    val showBottomBar = currentDestination?.let { dest ->
//        dest.hasRoute<HivesListRoute>() ||
//                dest.hasRoute<SettingsRoute>()
//        // || dest.hasRoute<QueenListRoute>() // Добавьте другие главные экраны
//    } ?: false
//
//    Scaffold(
//        modifier = Modifier.fillMaxSize(),
//        bottomBar = {
//            if (showBottomBar) {
//                AppBottomBar(
//                    currentDestination = currentDestination,
//                    onNavigate = { route ->
//                        navController.navigate(route) {
//                            popUpTo(navController.graph.findStartDestination().id) {
//                                saveState = true
//                            }
//                            launchSingleTop = true
//                            restoreState = true
//                        }
//                    }
//                )
//            }
//        }
//    ) { innerPadding ->
//        AppNavigation(
//            modifier = Modifier.padding(innerPadding),
//            navController = navController
//        )
//    }
//}

@Composable
fun AppHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = currentDestination?.let { dest ->
        dest.hasRoute<HivesListRoute>() || dest.hasRoute<SettingsRoute>()
    } ?: false

    Scaffold(
        modifier = Modifier.fillMaxSize(),

        bottomBar = {}
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
        ) {

            AppNavigation(
                modifier = Modifier.fillMaxSize(),
                navController = navController
            )

            Column(
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                AnimatedVisibility(
                    visible = showBottomBar,
                    enter = slideInVertically { it },
                    exit = slideOutVertically { it }
                ) {
                    AppBottomBar(
                        currentDestination = currentDestination,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    }
}
