package com.squirtles.musicroad.pick.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.squirtles.domain.model.Song

@Composable
internal fun SongInfo(
    song: Song,
    dynamicOnBackgroundColor: Color
) {
    Text(
        text = song.songName,
        color = dynamicOnBackgroundColor,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
    )

    Text(
        text = song.artistName,
        color = dynamicOnBackgroundColor,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodyLarge
    )
}
