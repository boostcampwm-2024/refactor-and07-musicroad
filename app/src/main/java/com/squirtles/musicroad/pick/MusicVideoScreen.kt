package com.squirtles.musicroad.pick

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.squirtles.musicroad.ui.theme.Purple15
import com.squirtles.musicroad.ui.theme.White

@Composable
fun MusicVideoScreen(
    modifier: Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Purple15),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "다음 화면", color = White, fontSize = 24.sp)
    }
}
