package com.squirtles.musicroad.map.components

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import com.squirtles.musicroad.R
import com.squirtles.musicroad.ui.theme.Primary

@Composable
fun SongInfoText(
    songInfo: String
) {
    Text(
        text = songInfo,
        color = MaterialTheme.colorScheme.onSurface,
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
        style = MaterialTheme.typography.titleMedium,
    )
}

@Composable
fun CreatedBySelfText(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    style: TextStyle = MaterialTheme.typography.bodyMedium
) {
    Text(
        text = stringResource(R.string.map_pick_created_by_self),
        modifier = modifier,
        color = color,
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
        style = style,
    )
}

@Composable
fun CreatedByOtherUserText(
    userName: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    style: TextStyle = MaterialTheme.typography.bodyMedium
) {
    Text(
        text = userName,
        modifier = modifier,
        color = color,
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
        style = style,
    )

    Text(
        text = stringResource(id = R.string.map_info_window_pick_user),
        color = color,
        style = style,
    )
}

@Composable
fun FavoriteCountText(
    favoriteCount: Int,
    color: Color = MaterialTheme.colorScheme.onSurface,
    style: TextStyle = MaterialTheme.typography.bodyMedium
) {
    Icon(
        painter = painterResource(id = R.drawable.ic_favorite),
        contentDescription = stringResource(R.string.map_info_window_favorite_count_icon_description),
        tint = Primary
    )

    Text(
        text = " $favoriteCount",
        color = color,
        style = style,
    )
}
