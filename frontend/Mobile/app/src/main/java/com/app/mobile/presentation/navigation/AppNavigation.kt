package com.app.mobile.presentation.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.app.mobile.presentation.ui.animations.NavEnterTransition
import com.app.mobile.presentation.ui.animations.NavExitTransition
import com.app.mobile.presentation.ui.animations.NavPopEnterTransition
import com.app.mobile.presentation.ui.animations.NavPopExitTransition
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.app.mobile.presentation.ui.screens.aboutapp.AboutAppRoute
import com.app.mobile.presentation.ui.screens.aboutapp.AboutAppScreen
import com.app.mobile.presentation.ui.screens.aboutapp.viewmodel.AboutAppViewModel
import com.app.mobile.presentation.ui.screens.main.HomeScreen
import com.app.mobile.presentation.ui.screens.main.MainRoute
import com.app.mobile.presentation.ui.screens.main.viewmodel.HomeViewModel
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
import com.app.mobile.presentation.ui.screens.hub.details.HubRoute
import com.app.mobile.presentation.ui.screens.hub.details.HubScreen
import com.app.mobile.presentation.ui.screens.hub.details.viewmodel.HubViewModel
import com.app.mobile.presentation.ui.screens.hub.editor.HubEditorRoute
import com.app.mobile.presentation.ui.screens.hub.editor.HubEditorScreen
import com.app.mobile.presentation.ui.screens.hub.editor.viewmodel.HubEditorViewModel
import com.app.mobile.presentation.ui.screens.hub.list.HubsListRoute
import com.app.mobile.presentation.ui.screens.hub.list.HubsListScreen
import com.app.mobile.presentation.ui.screens.hub.list.viewmodel.HubsListViewModel
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
import com.app.mobile.presentation.ui.screens.sensorchart.SensorChartRoute
import com.app.mobile.presentation.ui.screens.sensorchart.SensorChartScreen
import com.app.mobile.presentation.ui.screens.sensorchart.viewmodel.SensorChartViewModel
import com.app.mobile.presentation.ui.screens.registration.RegistrationScreen
import com.app.mobile.presentation.ui.screens.registration.viewmodel.RegistrationViewModel
import com.app.mobile.presentation.ui.screens.settings.SettingsRoute
import com.app.mobile.presentation.ui.screens.settings.SettingsScreen
import com.app.mobile.presentation.ui.screens.settings.viewmodel.SettingsViewModel
import com.app.mobile.presentation.ui.screens.works.detail.WorkDetailRoute
import com.app.mobile.presentation.ui.screens.works.detail.WorkDetailScreen
import com.app.mobile.presentation.ui.screens.works.detail.viewmodel.WorkDetailViewModel
import com.app.mobile.presentation.ui.screens.works.editor.WorkEditorRoute
import com.app.mobile.presentation.ui.screens.works.editor.WorksEditorScreen
import com.app.mobile.presentation.ui.screens.works.editor.viewmodel.WorksEditorViewModel
import com.app.mobile.presentation.ui.screens.works.list.WorksListRoute
import com.app.mobile.presentation.ui.screens.works.list.WorksListScreen
import com.app.mobile.presentation.ui.screens.works.list.viewmodel.WorksListViewModel
import org.koin.androidx.compose.koinViewModel


@Composable
fun AppNavigation(
    startDestination: Any,
    modifier: Modifier = Modifier,
    navController: NavHostController,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        animatedComposable<MainRoute> {
            val homeViewModel: HomeViewModel = koinViewModel()
            HomeScreen(
                homeViewModel = homeViewModel,
                onHiveClick = { navController.navigate(HiveRoute(it)) },
                onQueenClick = { navController.navigate(QueenRoute(it)) },
                onHubClick = { navController.navigate(HubRoute(it)) },
                onWorkClick = { workId, hiveId -> navController.navigate(WorkDetailRoute(workId, hiveId)) }
            )
        }

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
                onAuthorizeClick = { navController.navigate(MainRoute) },
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

        animatedComposable<HubsListRoute> {
            val hubsListViewModel: HubsListViewModel = koinViewModel()
            HubsListScreen(
                hubsListViewModel,
                onHubClick = { navController.navigate(HubRoute(it)) },
                onCreateHubClick = { navController.navigate(HubEditorRoute(null)) }
            )
        }

        animatedComposable<HubRoute> {
            val hubViewModel: HubViewModel = koinViewModel()
            HubScreen(
                hubViewModel,
                onHubListClick = { navController.popBackStack() },
                onHubEditClick = { hubId -> navController.navigate(HubEditorRoute(hubId)) },
                onNotificationsClick = { TODO("NotificationsRoute") },
                onSensorChartClick = { hubId, sensorType, hubName, currentValue ->
                    navController.navigate(SensorChartRoute(hubId, sensorType, hubName, currentValue))
                }
            )
        }

        animatedComposable<HubEditorRoute> {
            val hubEditorViewModel: HubEditorViewModel = koinViewModel()
            HubEditorScreen(
                hubEditorViewModel,
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
            val route = it.toRoute<HiveRoute>()
            val hiveViewModel: HiveViewModel = koinViewModel()
            HiveScreen(
                hiveViewModel,
                onQueenClick = { queenName ->
                    navController.navigate(QueenRoute(queenName, fromHiveName = route.hiveName))
                },
                onWorksClick = { navController.navigate(WorksListRoute(it)) },
                onWorkDetailClick = { workId, hiveName ->
                    navController.navigate(WorkDetailRoute(workId, hiveName))
                },
                onNotificationsClick = { TODO("NotificationsRoute") },
                onTemperatureClick = { hubId, hubName, currentValue ->
                    navController.navigate(SensorChartRoute(hubId, "temperature", hubName, currentValue))
                },
                onNoiseClick = { hubId, hubName, currentValue ->
                    navController.navigate(SensorChartRoute(hubId, "noise", hubName, currentValue))
                },
                onWeightClick = { hubId, hubName, currentValue ->
                    navController.navigate(SensorChartRoute(hubId, "weight", hubName, currentValue))
                },
                onHiveListClick = { navController.navigate(HivesListRoute) },
                onHiveEditClick = { hiveName -> navController.navigate(HiveEditorRoute(hiveName)) }
            )
        }

        animatedComposable<QueenRoute> {
            val queenViewModel: QueenViewModel = koinViewModel()
            QueenScreen(
                queenViewModel,
                onEditClick = { queenName ->
                    navController.navigate(QueenEditorRoute(queenName))
                },
                onHiveClick = { hiveName ->
                    navController.navigate(HiveRoute(hiveName))
                },
                onBackClick = {
                    navController.popBackStack()
                },
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
                onCreateHubClick = { navController.navigate(HubEditorRoute(null)) }
            )
        }

        animatedComposable<WorksListRoute> {
            val worksListViewModel: WorksListViewModel = koinViewModel()
            WorksListScreen(
                worksListViewModel,
                onWorkClick = { workId, hiveId ->
                    navController.navigate(WorkDetailRoute(workId, hiveId))
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

        animatedComposable<WorkDetailRoute> {
            val workDetailViewModel: WorkDetailViewModel = koinViewModel()
            WorkDetailScreen(
                workDetailViewModel,
                onEditClick = { workId, hiveId ->
                    navController.navigate(WorkEditorRoute(workId = workId, hiveId = hiveId))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        animatedComposable<SensorChartRoute> {
            val sensorChartViewModel: SensorChartViewModel = koinViewModel()
            SensorChartScreen(
                sensorChartViewModel,
                onBackClick = { navController.popBackStack() }
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
    noinline enterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? = NavEnterTransition,
    noinline exitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? = NavExitTransition,
    noinline popEnterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? = NavPopEnterTransition,
    noinline popExitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? = NavPopExitTransition,
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
