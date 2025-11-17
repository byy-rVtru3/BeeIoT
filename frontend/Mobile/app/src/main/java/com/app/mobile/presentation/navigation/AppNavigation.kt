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
import com.app.mobile.presentation.ui.screens.authorization.AuthorizationRoute
import com.app.mobile.presentation.ui.screens.authorization.AuthorizationScreen
import com.app.mobile.presentation.ui.screens.authorization.viewmodel.AuthorizationViewModel
import com.app.mobile.presentation.ui.screens.confirmation.ConfirmationRoute
import com.app.mobile.presentation.ui.screens.confirmation.ConfirmationScreen
import com.app.mobile.presentation.ui.screens.confirmation.viewmodel.ConfirmationViewModel
import com.app.mobile.presentation.ui.screens.registration.RegistrationRoute
import com.app.mobile.presentation.ui.screens.registration.RegistrationScreen
import com.app.mobile.presentation.ui.screens.registration.viewmodel.RegistrationViewModel
import org.koin.androidx.compose.koinViewModel


@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController,
) {
    NavHost(
        navController = navController,
        startDestination = RegistrationRoute,
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
                confirmationViewModel, destination.email, destination.type,
                onConfirmClick = {
                    navController.navigate(AuthorizationRoute)
                }
            )
        }

        animatedComposable<AuthorizationRoute> {
            val authorizationViewModel: AuthorizationViewModel = koinViewModel()
            AuthorizationScreen(
                authorizationViewModel,
                onAuthorizeClick = { TODO("add navigation to main screen") })
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
