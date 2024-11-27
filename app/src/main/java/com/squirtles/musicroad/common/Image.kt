package com.squirtles.musicroad.common

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.squirtles.musicroad.R
import com.squirtles.musicroad.ui.theme.Gray

@Composable
fun AlbumImage(
    imageUrl: String?,
    imageSize: Dp,
    roundedCornerSize: Dp = 4.dp,
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .crossfade(true)
            .build(),
        contentDescription = stringResource(R.string.map_album_image_description),
        modifier = Modifier
            .size(imageSize)
            .clip(RoundedCornerShape(roundedCornerSize)),
        placeholder = ColorPainter(Gray),
        contentScale = ContentScale.Crop,
    )
}
