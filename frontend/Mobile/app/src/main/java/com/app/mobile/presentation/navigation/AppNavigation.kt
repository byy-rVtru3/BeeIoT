package com.app.mobile.presentation.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
import com.app.mobile.presentation.ui.screens.hive.details.HiveRoute
import com.app.mobile.presentation.ui.screens.hive.details.HiveScreen
import com.app.mobile.presentation.ui.screens.hive.details.viewmodel.HiveViewModel
import com.app.mobile.presentation.ui.screens.hive.editor.HiveEditorRoute
import com.app.mobile.presentation.ui.screens.hive.editor.HiveEditorScreen
import com.app.mobile.presentation.ui.screens.hive.editor.viewmodel.HiveEditorViewModel
import com.app.mobile.presentation.ui.screens.hive.list.HivesListRoute
import com.app.mobile.presentation.ui.screens.hive.list.HivesListScreen
import com.app.mobile.presentation.ui.screens.hive.list.vewmodel.HivesListViewModel
import com.app.mobile.presentation.ui.screens.queen.details.QueenRoute
import com.app.mobile.presentation.ui.screens.queen.details.QueenScreen
import com.app.mobile.presentation.ui.screens.queen.details.viewmodel.QueenViewModel
import com.app.mobile.presentation.ui.screens.queen.editor.QueenEditorRoute
import com.app.mobile.presentation.ui.screens.queen.editor.QueenEditorScreen
import com.app.mobile.presentation.ui.screens.queen.editor.viewmodel.QueenEditorViewModel
import com.app.mobile.presentation.ui.screens.queen.list.QueenListRoute
import com.app.mobile.presentation.ui.screens.queen.list.QueenListScreen
import com.app.mobile.presentation.ui.screens.queen.list.viewmodel.QueenListViewModel
import com.app.mobile.presentation.ui.screens.registration.RegistrationRoute
import com.app.mobile.presentation.ui.screens.registration.RegistrationScreen
import com.app.mobile.presentation.ui.screens.registration.viewmodel.RegistrationViewModel
import com.app.mobile.presentation.ui.screens.settings.SettingsRoute
import com.app.mobile.presentation.ui.screens.settings.SettingsScreen
import com.app.mobile.presentation.ui.screens.settings.viewmodel.SettingsViewModel
import com.app.mobile.presentation.ui.screens.works.editor.WorkEditorRoute
import com.app.mobile.presentation.ui.screens.works.editor.WorksEditorScreen
import com.app.mobile.presentation.ui.screens.works.editor.viewmodel.WorksEditorViewModel
import com.app.mobile.presentation.ui.screens.works.list.WorksListRoute
import com.app.mobile.presentation.ui.screens.works.list.WorksListScreen
import com.app.mobile.presentation.ui.screens.works.list.viewmodel.WorksListViewModel
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
            val confirmationViewModel: ConfirmationViewModel = koinViewModel()
            ConfirmationScreen(
                confirmationViewModel,
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
                onDeleteClick = { navController.navigate(AuthorizationRoute) },
                onBackClick = { navController.popBackStack() }
            )
        }

        animatedComposable<AboutAppRoute> {
            val aboutAppViewModel: AboutAppViewModel = koinViewModel()
            AboutAppScreen(
                aboutAppViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        animatedComposable<HivesListRoute> {
            val hivesListViewModel: HivesListViewModel = koinViewModel()
            HivesListScreen(
                hivesListViewModel,
                onHiveClick = { navController.navigate(HiveRoute(it)) },
                onCreateHiveClick = { navController.navigate(HiveEditorRoute(null)) }
            )
        }

        animatedComposable<HiveRoute> {
            val hiveViewModel: HiveViewModel = koinViewModel()
            HiveScreen(
                hiveViewModel,
                onQueenClick = { navController.navigate(QueenRoute) },
                onWorksClick = { navController.navigate(WorksListRoute(it)) },
                onNotificationsClick = { TODO("NotificationsRoute") },
                onTemperatureClick = { TODO("TemperatureRoute") },
                onNoiseClick = { TODO("NoiseRoute") },
                onWeightClick = { TODO("WeightRoute") },
                onHiveListClick = { navController.navigate(HivesListRoute) },
                onHiveEditClick = { navController.navigate(HiveEditorRoute) }
            )
        }

        animatedComposable<QueenRoute> {
            val queenViewModel: QueenViewModel = koinViewModel()
            QueenScreen(
                queenViewModel,
                onEditClick = {},
                onHiveClick = {}
            )
        }

        animatedComposable<QueenEditorRoute> {
            val queenEditorViewModel: QueenEditorViewModel = koinViewModel()
            QueenEditorScreen(
                queenEditorViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        animatedComposable<QueenListRoute> {
            val queenListViewModel: QueenListViewModel = koinViewModel()
            QueenListScreen(
                queenListViewModel,
                onQueenClick = { navController.navigate(QueenRoute(it)) },
                onAddClick = { navController.navigate(QueenEditorRoute(null)) }
            )
        }

        animatedComposable<HiveEditorRoute> {
            val hiveEditorViewModel: HiveEditorViewModel = koinViewModel()
            HiveEditorScreen(
                hiveEditorViewModel,
                onBackClick = { navController.popBackStack() },
                onCreateQueenClick = { navController.navigate(QueenEditorRoute(null)) },
                onCreateHubClick = { TODO("HiveCreateHubRoute") }
            )
        }

        animatedComposable<WorksListRoute> {
            val worksListViewModel: WorksListViewModel = koinViewModel()
            WorksListScreen(
                worksListViewModel,
                onWorkClick = { workId, hiveId ->
                    navController.navigate(
                        WorkEditorRoute(workId = workId, hiveId = hiveId)
                    )
                },
                onCreateClick = { hiveId ->
                    navController.navigate(
                        WorkEditorRoute(workId = null, hiveId = hiveId)
                    )
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        animatedComposable<WorkEditorRoute> {
            val worksEditorViewModel: WorksEditorViewModel = koinViewModel()
            WorksEditorScreen(
                worksEditorViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

inline fun <reified T : Any> NavGraphBuilder.animatedComposable(
    noinline enterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? = {
        fadeIn(animationSpec = tween(300))
    },
    noinline exitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? = {
        fadeOut(animationSpec = tween(300))
    },
    noinline popEnterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? = enterTransition,
    noinline popExitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? = exitTransition,

    noinline block: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
) {
    composable<T>(
        enterTransition = enterTransition,
        exitTransition = exitTransition,
        popEnterTransition = popEnterTransition,
        popExitTransition = popExitTransition,
        content = block
    )
}