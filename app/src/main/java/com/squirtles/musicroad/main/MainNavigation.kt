package com.squirtles.musicroad.main

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

object MainDestinations {
    const val MAIN_ROUTE = "main"
    const val FAVORITE_ROUTE = "favorite"
    const val SETTING_ROUTE = "setting"
    const val SEARCH_ROUTE = "search"
}

object CreatePickDestinations {
    const val CREATE_ROUTE = "create"
    const val SEARCH_MUSIC_ROUTE = "search_music"
    const val CREATE_PICK_ROUTE = "create_pick"
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

    val navigateToSetting: () -> Unit = {
        navController.navigate(MainDestinations.SETTING_ROUTE) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    val navigateToSearch: () -> Unit = {
        navController.navigate(MainDestinations.SEARCH_ROUTE) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }
}
