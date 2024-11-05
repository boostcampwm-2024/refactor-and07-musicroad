package com.squirtles.musicroad.main

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.squirtles.musicroad.favorite.FavoriteScreen
import com.squirtles.musicroad.map.MapScreen

@Composable
fun MainNavGraph(
    navController: NavHostController,
    navigationActions: MainNavigationActions,
    modifier: Modifier = Modifier,
    startDestination: String = MainDestinations.MAIN_ROUTE,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(MainDestinations.MAIN_ROUTE) {
            MapScreen()
        }

        composable(MainDestinations.FAVORITE_ROUTE) {
            FavoriteScreen()
        }
    }
}