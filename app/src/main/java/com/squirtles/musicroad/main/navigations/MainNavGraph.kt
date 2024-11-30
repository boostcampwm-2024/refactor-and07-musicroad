package com.squirtles.musicroad.main.navigations

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.squirtles.musicroad.create.CreatePickScreen
import com.squirtles.musicroad.create.CreatePickViewModel
import com.squirtles.musicroad.create.SearchMusicScreen
import com.squirtles.musicroad.favorite.FavoriteScreen
import com.squirtles.musicroad.map.MapScreen
import com.squirtles.musicroad.map.MapViewModel
import com.squirtles.musicroad.pick.DetailPickScreen
import com.squirtles.musicroad.setting.SettingScreen
import com.squirtles.musicroad.videoplayer.MusicVideoScreen
import com.squirtles.musicroad.videoplayer.VideoPlayerViewModel

@Composable
fun MainNavGraph(
    modifier: Modifier = Modifier,
    mapViewModel: MapViewModel = hiltViewModel(),
    navController: NavHostController,
    navigationActions: MainNavigationActions,
) {
    NavHost(
        navController = navController,
        startDestination = MainDestinations.MAIN_ROUTE,
        modifier = modifier
    ) {
        composable(MainDestinations.MAIN_ROUTE) {
            MapScreen(
                mapViewModel = mapViewModel,
                onFavoriteClick = {
                    // FIXME: 임시로 화면 전환 막아놓음
                },
                onCenterClick = navigationActions.navigateToSearch,
                onSettingClick = {
                    // FIXME: 임시로 화면 전환 막아놓음
                },
                onPickSummaryClick = { pickId ->
                    navigationActions.navigateToPickDetail(pickId)
                },
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
                    },
                    onCreateClick = { pickId ->
                        navigationActions.navigateToPickDetail(pickId)
                    }
                )
            }
        }

        navigation(
            startDestination = PickInfoDestinations.pickDetail("{pickId}"),
            route = PickInfoDestinations.PICK_DETAIL_ROUTE
        ) {
            composable(
                route = PickInfoDestinations.pickDetail("{pickId}"),
                arguments = listOf(navArgument("pickId") { type = NavType.StringType })
            ) { backStackEntry ->
                val pickId = backStackEntry.arguments?.getString("pickId") ?: ""
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(PickInfoDestinations.PICK_DETAIL_ROUTE)
                }

                DetailPickScreen(
                    pickId = pickId,
                    videoPlayerViewModel = hiltViewModel<VideoPlayerViewModel>(parentEntry),
                    onBackClick = { navController.navigateUp() },
                    onDeleted = { mapViewModel.resetClickedMarkerState(it) },
                    onMusicVideoClick = { navController.navigate(PickInfoDestinations.MUSIC_VIDEO_ROUTE) }
                )
            }

            composable(
                route = PickInfoDestinations.MUSIC_VIDEO_ROUTE,
                enterTransition = {
                    fadeIn(
                        animationSpec = tween(
                            300, easing = LinearEasing
                        )
                    ) + slideIntoContainer(
                        animationSpec = tween(300, easing = EaseIn),
                        towards = AnimatedContentTransitionScope.SlideDirection.Start
                    )
                },
                exitTransition = {
                    fadeOut(
                        animationSpec = tween(
                            300, easing = LinearEasing
                        )
                    ) + slideOutOfContainer(
                        animationSpec = tween(300, easing = EaseOut),
                        towards = AnimatedContentTransitionScope.SlideDirection.End
                    )
                }
            ) {
                val parentEntry = remember(it) {
                    navController.getBackStackEntry(PickInfoDestinations.PICK_DETAIL_ROUTE)
                }

                MusicVideoScreen(
                    videoPlayerViewModel = hiltViewModel<VideoPlayerViewModel>(parentEntry),
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
