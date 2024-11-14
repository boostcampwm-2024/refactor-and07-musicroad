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
import com.squirtles.musicroad.create.CreatePickScreen
import com.squirtles.musicroad.create.CreatePickViewModel
import com.squirtles.musicroad.create.SearchMusicScreen
import com.squirtles.musicroad.setting.SettingScreen

@Composable
fun MainNavGraph(
    mapViewModel: MapViewModel,
    navController: NavHostController,
    navigationActions: MainNavigationActions,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = MainDestinations.MAIN_ROUTE,
        modifier = modifier
    ) {
        composable(MainDestinations.MAIN_ROUTE) {
            MapScreen(
                mapViewModel = mapViewModel,
                onFavoriteClick = navigationActions.navigateToFavorite,
                onCenterClick = navigationActions.navigateToSearch,
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
                    createPickViewModel = hiltViewModel<CreatePickViewModel>(parentEntry),
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onItemClick = navigationActions.navigateToCreate
                )
            }
            composable(CreatePickDestinations.CREATE_PICK_ROUTE) {
                val parentEntry = remember(it) {
                    navController.getBackStackEntry(CreatePickDestinations.SEARCH_ROUTE)
                }
                CreatePickScreen(
                    createPickViewModel = hiltViewModel<CreatePickViewModel>(parentEntry),
                    onBackClick = {
                        navController.popBackStack()
                    }
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
