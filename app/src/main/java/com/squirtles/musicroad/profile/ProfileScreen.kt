package com.squirtles.musicroad.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.SwitchAccount
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.squirtles.musicroad.R
import com.squirtles.musicroad.common.Constants.COLOR_STOPS
import com.squirtles.musicroad.common.DefaultTopAppBar
import com.squirtles.musicroad.common.VerticalSpacer

@Composable
fun ProfileScreen(
    userId: String,
    onBackClick: () -> Unit,
    onFavoritePicksClick: () -> Unit,
    onMyPicksClick: () -> Unit,
    onSettingProfileClick: () -> Unit,
    onSettingNotificationClick: () -> Unit,
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    val user by profileViewModel.profileUser.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        profileViewModel.getUserById(userId)
    }

    Scaffold(
        topBar = {
            DefaultTopAppBar(
                title = user?.userName ?: "",
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(colorStops = COLOR_STOPS))
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier.verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                VerticalSpacer(16)

                Image(
                    painter = painterResource(R.drawable.img_user_default_profile),
                    contentDescription = stringResource(R.string.user_info_default_profile_image),
                    modifier = Modifier
                        .size(180.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                VerticalSpacer(40)

                ProfileMenus(
                    title = stringResource(R.string.user_info_pick_category_title),
                    menus = listOf(
                        MenuItem(
                            imageVector = Icons.Outlined.Archive,
                            contentDescription = stringResource(R.string.user_info_favorite_menu_icon_description),
                            menuTitle = stringResource(R.string.user_info_favorite_menu_title),
                            onMenuClick = onFavoritePicksClick
                        ),
                        MenuItem(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = stringResource(R.string.user_info_created_by_self_menu_icon_description),
                            menuTitle = stringResource(R.string.user_info_created_by_self_menu_title),
                            onMenuClick = onMyPicksClick
                        )
                    )
                )

                if (userId == profileViewModel.currentUser.userId) {
                    ProfileMenus(
                        title = stringResource(R.string.user_info_setting_category_title),
                        menus = listOf(
                            MenuItem(
                                imageVector = Icons.Outlined.SwitchAccount,
                                contentDescription = stringResource(R.string.user_info_setting_profile_menu_icon_description),
                                menuTitle = stringResource(R.string.user_info_setting_profile_menu_title),
                                onMenuClick = onSettingProfileClick
                            ),
                            MenuItem(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = stringResource(R.string.user_info_setting_notification_menu_icon_description),
                                menuTitle = stringResource(R.string.user_info_setting_notification_menu_title),
                                onMenuClick = onSettingNotificationClick
                            )
                        )
                    )
                }
            }
        }
    }
}
