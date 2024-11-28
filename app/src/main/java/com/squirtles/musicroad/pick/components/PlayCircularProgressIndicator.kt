package com.squirtles.musicroad.pick.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.squirtles.musicroad.ui.theme.White
import kotlin.math.atan2

@Composable
internal fun PlayCircularProgressIndicator(
    modifier: Modifier = Modifier,
    currentTime: () -> Float,
    strokeWidth: Dp,
    duration: Float,
    onSeekChanged: (Float) -> Unit
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val centerX = size.width / 2
                    val centerY = size.height / 2

                    // 중심 기준 터치좌표의 각도
                    val angle = Math
                        .toDegrees(
                            atan2(offset.y - centerY, offset.x - centerX).toDouble()
                        )
                        .toFloat()

                    // 원의 위쪽 점(12시 방향)을 기준으로 시계 방향
                    val normalizedAngle = ((angle + 360) % 360 + 90) % 360

                    // 각도를 재생 시간으로 변환
                    val seekTime = (normalizedAngle / 360f) * duration
                    onSeekChanged(seekTime)
                }
            }
    ) {
        CircularProgressIndicator(
            modifier = Modifier.fillMaxSize(),
            progress = { currentTime() / duration },
            color = White,
            trackColor = Color.Transparent,
            strokeWidth = strokeWidth,
            strokeCap = StrokeCap.Round,
            gapSize = 0.dp,
        )
    }
}

