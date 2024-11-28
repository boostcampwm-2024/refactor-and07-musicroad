package com.squirtles.musicroad.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import com.squirtles.musicroad.R

@Composable
fun CreatedBySelfText(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    style: TextStyle = MaterialTheme.typography.bodyMedium
) {
    Text(
        text = stringResource(R.string.pick_created_by_self),
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
