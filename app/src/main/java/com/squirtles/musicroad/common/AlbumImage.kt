package com.squirtles.musicroad.common

import android.util.Size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.squirtles.musicroad.R
import com.squirtles.musicroad.ui.theme.Gray

@Composable
fun AlbumImage(
    imageUrl: String?,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .crossfade(true)
            .build(),
        contentDescription = stringResource(R.string.map_album_image_description),
        modifier = modifier,
        placeholder = ColorPainter(Gray),
        error = ColorPainter(Gray),
        contentScale = ContentScale.Crop,
    )
}

fun String.toImageUrlWithSize(size: Size): String? {
    return if (isEmpty()) null
    else replace("{w}", size.width.toString())
        .replace("{h}", size.height.toString())
}
