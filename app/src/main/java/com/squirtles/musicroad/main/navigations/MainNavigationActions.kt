package com.squirtles.musicroad.main.navigations

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

class MainNavigationActions(navController: NavHostController) {
    val navigateToMain: () -> Unit = {
        navController.navigate(MainDestinations.MAIN_ROUTE) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

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
        navController.navigate(CreatePickDestinations.SEARCH_ROUTE) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
        }
    }

    val navigateToCreate: () -> Unit = {
        navController.navigate(CreatePickDestinations.CREATE_PICK_ROUTE) {
            launchSingleTop = true
        }
    }

    val navigateToPickDetail: (String) -> Unit = { pickId ->
        navController.navigate(PickInfoDestinations.pickDetail(pickId)) {
            launchSingleTop = true
        }
    }
}
