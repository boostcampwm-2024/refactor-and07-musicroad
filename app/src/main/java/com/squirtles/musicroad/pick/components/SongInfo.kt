package com.squirtles.musicroad.pick.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.squirtles.domain.model.Song

@Composable
internal fun SongInfo(
    song: Song,
    dynamicOnBackgroundColor: Color
) {
    Text(
        text = song.songName,
        color = dynamicOnBackgroundColor,
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
    )

    Text(
        text = song.artistName,
        color = dynamicOnBackgroundColor,
        style = MaterialTheme.typography.bodyLarge
    )
}
