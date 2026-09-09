package com.fiestast.launcher.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.fiestast.launcher.ui.screens.HomeScreen
import com.fiestast.launcher.ui.screens.PlaceholderScreen
import com.fiestast.launcher.ui.viewmodel.LauncherViewModel

@Composable
fun LauncherNavHost(
    navController: NavHostController,
    viewModel: LauncherViewModel,
    modifier: Modifier = Modifier
) {
    val onNavigate: (String) -> Unit = { targetRoute ->
        if (targetRoute == NavRoutes.ZLINK) {
            viewModel.launchZLink()
        }
        if (navController.currentDestination?.route != targetRoute) {
            navController.navigate(targetRoute) {
                // Pop up to home to avoid backstack bloat on automotive launcher
                popUpTo(NavRoutes.HOME) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = NavRoutes.HOME,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        modifier = modifier.fillMaxSize()
    ) {
        composable(NavRoutes.HOME) {
            HomeScreen(
                viewModel = viewModel,
                onNavigate = onNavigate
            )
        }

        NavRoutes.allRoutes.filter { it != NavRoutes.HOME }.forEach { route ->
            composable(route) {
                PlaceholderScreen(
                    route = route,
                    viewModel = viewModel,
                    onNavigate = onNavigate
                )
            }
        }
    }
}
