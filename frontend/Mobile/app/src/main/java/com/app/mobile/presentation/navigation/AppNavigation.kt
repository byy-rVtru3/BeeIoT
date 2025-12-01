package com.app.mobile.presentation.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.app.mobile.presentation.ui.screens.aboutapp.AboutAppRoute
import com.app.mobile.presentation.ui.screens.aboutapp.AboutAppScreen
import com.app.mobile.presentation.ui.screens.aboutapp.viewmodel.AboutAppViewModel
import com.app.mobile.presentation.ui.screens.accountinfo.AccountInfoRoute
import com.app.mobile.presentation.ui.screens.accountinfo.AccountInfoScreen
import com.app.mobile.presentation.ui.screens.accountinfo.viewmodel.AccountInfoViewModel
import com.app.mobile.presentation.ui.screens.authorization.AuthorizationRoute
import com.app.mobile.presentation.ui.screens.authorization.AuthorizationScreen
import com.app.mobile.presentation.ui.screens.authorization.viewmodel.AuthorizationViewModel
import com.app.mobile.presentation.ui.screens.confirmation.ConfirmationRoute
import com.app.mobile.presentation.ui.screens.confirmation.ConfirmationScreen
import com.app.mobile.presentation.ui.screens.confirmation.viewmodel.ConfirmationViewModel
import com.app.mobile.presentation.ui.screens.hive.HiveRoute
import com.app.mobile.presentation.ui.screens.hive.HiveScreen
import com.app.mobile.presentation.ui.screens.hive.viewmodel.HiveViewModel
import com.app.mobile.presentation.ui.screens.hives.list.HivesListRoute
import com.app.mobile.presentation.ui.screens.hives.list.HivesListScreen
import com.app.mobile.presentation.ui.screens.hives.list.vewmodel.HivesListViewModel
import com.app.mobile.presentation.ui.screens.registration.RegistrationRoute
import com.app.mobile.presentation.ui.screens.registration.RegistrationScreen
import com.app.mobile.presentation.ui.screens.registration.viewmodel.RegistrationViewModel
import com.app.mobile.presentation.ui.screens.settings.SettingsRoute
import com.app.mobile.presentation.ui.screens.settings.SettingsScreen
import com.app.mobile.presentation.ui.screens.settings.viewmodel.SettingsViewModel
import org.koin.androidx.compose.koinViewModel


@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController,
) {
    NavHost(
        navController = navController,
        startDestination = AuthorizationRoute,
        modifier = modifier
    ) {
        animatedComposable<RegistrationRoute> {
            val registrationViewModel: RegistrationViewModel = koinViewModel()
            RegistrationScreen(
                registrationViewModel = registrationViewModel,
                onRegisterClick = { email, type ->
                    navController.navigate(ConfirmationRoute(email, type))
                }
            )
        }

        animatedComposable<ConfirmationRoute> {
            val destination = it.toRoute<ConfirmationRoute>()
            val confirmationViewModel: ConfirmationViewModel = koinViewModel()
            ConfirmationScreen(
                confirmationViewModel,
                destination.email,
                destination.type,
                onConfirmClick = {
                    navController.navigate(AuthorizationRoute)
                }
            )
        }

        animatedComposable<AuthorizationRoute> {
            val authorizationViewModel: AuthorizationViewModel = koinViewModel()
            AuthorizationScreen(
                authorizationViewModel,
                onAuthorizeClick = { navController.navigate(SettingsRoute) },
                onRegistrationClick = { navController.navigate(RegistrationRoute) }
            )
        }

        animatedComposable<SettingsRoute> {
            val settingsViewModel: SettingsViewModel = koinViewModel()
            SettingsScreen(
                settingsViewModel,
                onAccountInfoClick = { navController.navigate(AccountInfoRoute) },
                onAboutAppClick = { navController.navigate(AboutAppRoute) },
                onLogoutClick = { navController.navigate(AuthorizationRoute) })
        }

        animatedComposable<AccountInfoRoute> {
            val accountInfoViewModel: AccountInfoViewModel = koinViewModel()
            AccountInfoScreen(
                accountInfoViewModel,
                onDeleteClick = { navController.navigate(AuthorizationRoute) })
        }

        animatedComposable<AboutAppRoute> {
            val aboutAppViewModel: AboutAppViewModel = koinViewModel()
            AboutAppScreen(aboutAppViewModel)
        }

        animatedComposable<HivesListRoute> {
            val hivesListViewModel: HivesListViewModel = koinViewModel()
            HivesListScreen(
                hivesListViewModel,
                onHiveClick = { navController.navigate(HiveRoute(it)) },
                onCreateHiveClick = {TODO("HiveCreateRoute")}
            )
        }

        animatedComposable<HiveRoute> {
            val destination = it.toRoute<HiveRoute>()
            val hiveViewModel: HiveViewModel = koinViewModel()
            HiveScreen(
                hiveViewModel,
                destination.hiveId,
                onQueenClick = { TODO("QueenRoute") },
                onWorksClick = { TODO("WorksRoute") },
                onNotificationsClick = {TODO("NotificationsRoute")},
                onTemperatureClick = {TODO("TemperatureRoute") },
                onNoiseClick = {TODO("NoiseRoute") },
                onWeightClick = {TODO("WeightRoute") },
                onHiveListClick = { navController.navigate(HivesListRoute) },
                onHiveEditClick = {TODO("HiveEditRoute")}
            )
        }

    }
}

inline fun <reified T : Any> NavGraphBuilder.animatedComposable(
    noinline block: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
) {
    composable<T>(
        enterTransition = ENTER_TRANSITION,
        exitTransition = EXIT_TRANSITION,
        popEnterTransition = POP_ENTER_TRANSITION,
        popExitTransition = POP_EXIT_TRANSITION,
        content = block
    )
}
