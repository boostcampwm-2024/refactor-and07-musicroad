package com.squirtles.musicroad.main

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

object MainDestinations {
    const val MAIN_ROUTE = "main"
    const val FAVORITE_ROUTE = "favorite"
}

class MainNavigationActions(navController: NavHostController) {
    val navigateToFavorite: () -> Unit = {
        navController.navigate(MainDestinations.FAVORITE_ROUTE) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }
}