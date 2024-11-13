package com.squirtles.musicroad.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.squirtles.musicroad.favorite.FavoriteScreen
import com.squirtles.musicroad.map.MapScreen
import com.squirtles.musicroad.map.MapViewModel
import com.squirtles.musicroad.pick.CreatePickScreen
import com.squirtles.musicroad.search.CreatePickViewModel
import com.squirtles.musicroad.search.SearchMusicScreen
import com.squirtles.musicroad.setting.SettingScreen

@Composable
fun MainNavGraph(
    mapViewModel: MapViewModel,
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
            MapScreen(
                mapViewModel = mapViewModel,
                onFavoriteClick = navigationActions.navigateToFavorite,
                onCenterClick = { navController.navigate(CreatePickDestinations.SEARCH_ROUTE) },
                onSettingClick = navigationActions.navigateToSetting
            )
        }

        composable(MainDestinations.FAVORITE_ROUTE) {
            FavoriteScreen()
        }

        composable(MainDestinations.SETTING_ROUTE) {
            SettingScreen()
        }

        navigation(
            startDestination = CreatePickDestinations.SEARCH_MUSIC_ROUTE,
            route = CreatePickDestinations.SEARCH_ROUTE
        ) {
            composable(CreatePickDestinations.SEARCH_MUSIC_ROUTE) {
                val parentEntry = remember(it) {
                    navController.getBackStackEntry(CreatePickDestinations.SEARCH_ROUTE)
                }
                SearchMusicScreen(
                    searchViewModel = hiltViewModel<CreatePickViewModel>(parentEntry),
                    onItemClick = { navController.navigate(CreatePickDestinations.CREATE_PICK_ROUTE) }
                )
            }
            composable(CreatePickDestinations.CREATE_PICK_ROUTE) {
                val parentEntry = remember(it) {
                    navController.getBackStackEntry(CreatePickDestinations.SEARCH_ROUTE)
                }
                CreatePickScreen(
                    searchViewModel = hiltViewModel<CreatePickViewModel>(parentEntry),
                    onBackClick = { }
                )
            }
        }
    }
}

@Composable
inline fun <reified T : ViewModel> NavBackStackEntry.sharedViewModel(navController: NavController): T {
    val navGraphRoute = destination.parent?.route ?: return viewModel()
    val parentEntry = remember(this) {
        navController.getBackStackEntry(navGraphRoute)
    }
    return viewModel(parentEntry)
}
