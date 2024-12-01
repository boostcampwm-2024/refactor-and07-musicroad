package com.squirtles.musicroad.common

import android.util.Size
import androidx.compose.ui.unit.dp
import com.squirtles.musicroad.ui.theme.Black
import com.squirtles.musicroad.ui.theme.Primary

internal object Constants {
    val DEFAULT_PADDING = 16.dp

    val REQUEST_IMAGE_SIZE_DEFAULT = Size(300, 300)

    val COLOR_STOPS = arrayOf(
        0.0f to Primary,
        0.25f to Black
    )
}