package com.squirtles.musicroad.main.navigations

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
import com.squirtles.musicroad.map.MapScreen
import com.squirtles.musicroad.map.MapViewModel
import com.squirtles.musicroad.pick.DetailPickScreen
import com.squirtles.musicroad.picklist.PickListScreen
import com.squirtles.musicroad.setting.SettingScreen
import com.squirtles.musicroad.userinfo.UserInfoScreen

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
                onFavoriteClick = navigationActions.navigateToFavoritePicks,
                onCenterClick = navigationActions.navigateToSearch,
                onUserInfoClick = navigationActions.navigateToUserInfo,
                onPickSummaryClick = { pickId ->
                    navigationActions.navigateToPickDetail(pickId)
                },
            )
        }

        composable(MainDestinations.FAVORITE_PICKS_ROUTE) {
            PickListScreen(
                isFavoritePicks = true,
                onBackClick = { navController.navigateUp() },
                onItemClick = { pickId ->
                    navigationActions.navigateToPickDetail(pickId)
                }
            )
        }

        composable(MainDestinations.MY_PICKS_ROUTE) {
            PickListScreen(
                isFavoritePicks = false,
                onBackClick = { navController.navigateUp() },
                onItemClick = { pickId ->
                    navigationActions.navigateToPickDetail(pickId)
                }
            )
        }

        composable(MainDestinations.USER_INFO_ROUTE) {
            UserInfoScreen(
                onBackClick = { navController.navigateUp() },
                onFavoritePicksClick = navigationActions.navigateToFavoritePicks,
                onMyPicksClick = navigationActions.navigateToMyPicks,
            )
        }

        composable(MainDestinations.SETTING_ROUTE) {
            SettingScreen()
        }

        navigation(
            startDestination = CreatePickDestinations.SEARCH_MUSIC_ROUTE,
            route = CreatePickDestinations.CREATE_ROUTE
        ) {
            composable(CreatePickDestinations.SEARCH_MUSIC_ROUTE) {
                val parentEntry = remember(it) {
                    navController.getBackStackEntry(CreatePickDestinations.CREATE_ROUTE)
                }
                SearchMusicScreen(
                    createPickViewModel = hiltViewModel<CreatePickViewModel>(parentEntry),
                    onBackClick = { navController.navigateUp() },
                    onItemClick = navigationActions.navigateToCreate
                )
            }
            composable(CreatePickDestinations.CREATE_PICK_ROUTE) {
                val parentEntry = remember(it) {
                    navController.getBackStackEntry(CreatePickDestinations.CREATE_ROUTE)
                }
                CreatePickScreen(
                    createPickViewModel = hiltViewModel<CreatePickViewModel>(parentEntry),
                    onBackClick = { navController.navigateUp() },
                    onCreateClick = { pickId ->
                        navigationActions.navigateToPickDetail(pickId)
                    }
                )
            }
        }

        composable(
            route = PickInfoDestinations.pickDetail("{pickId}"),
            arguments = listOf(navArgument("pickId") { type = NavType.StringType })
        ) { backStackEntry ->
            val pickId = backStackEntry.arguments?.getString("pickId") ?: ""
            DetailPickScreen(
                pickId = pickId,
                onBackClick = { // 픽  등록에서 정보 화면으로 간 것이라면 뒤로 가기 시 메인으로, 아니라면 이전 화면으로
                    if (navController.previousBackStackEntry?.destination?.route == CreatePickDestinations.CREATE_PICK_ROUTE) {
                        navigationActions.navigateToMain()
                    } else {
                        navController.navigateUp()
                    }
                },
                onDeleted = { mapViewModel.resetClickedMarkerState(it) },
            )
        }
    }
}
