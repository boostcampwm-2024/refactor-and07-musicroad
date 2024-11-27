package com.squirtles.musicroad.pick.components.music

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.util.lerp
import com.squirtles.musicroad.pick.components.music.BaseVisualizer.Companion.CAPTURE_SIZE
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

@Composable
fun CircleVisualizer(
    audioSessionId: Int,
    color: Color = Color.White,
    modifier: Modifier = Modifier
) {
//    val bytes by baseVisualizer.bytesFlow.collectAsState(initial = ByteArray(0))
    val baseVisualizer = remember { BaseVisualizer(audioSessionId) }
    val interpolatedBytes = remember { mutableStateListOf<Float>() }

    LaunchedEffect(Unit) {
        baseVisualizer.bytesFlow.collect { bytes ->
//            Log.d("CircleVisualizer", "Received bytes: ${bytes.size}")
            if (interpolatedBytes.isEmpty()) {
                interpolatedBytes.addAll(bytes.map { (-abs(it.toInt()) + 128).toFloat() })
            } else {
                for (i in bytes.indices) {
                    interpolatedBytes[i] = lerp(
                        start = interpolatedBytes[i],
                        stop = (-abs(bytes[i].toInt()) + 128).toFloat(),
                        fraction = 0.4f
                    )
                }
            }
            interpolatedBytes[0] = interpolatedBytes.last()
        }
    }

//    LaunchedEffect(Unit) {
//        baseVisualizer.bytesFlow.collect { bytes ->
//            Log.d("CircleVisualizer", "Received bytes: ${bytes.contentToString()}")
//        }
//    }

    DisposableEffect(Unit) {
        onDispose {
            baseVisualizer.release()
        }
    }

    CanvasCircle(
        color = color,
        modifier = modifier
    )

    CanvasSoundEffect(
        interpolatedBytes = interpolatedBytes,
        color = color,
        modifier = modifier
    )
}

@Composable
private fun CanvasCircle(
    color: Color,
    modifier: Modifier = Modifier
) {

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val radius = max(width, height) * 0.4f

        drawCircle(
            color = color,
            radius = radius,
            style = Stroke(width = 4f)
        )
    }
}

@Composable
private fun CanvasSoundEffect(
    interpolatedBytes: List<Float>,
    color: Color,
    modifier: Modifier = Modifier
) {
    Log.d("CanvasSoundEffect", "${interpolatedBytes.size}")
    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val radius = max(width, height) * 0.4f
        val angleStep = 360f / CAPTURE_SIZE

        val points = mutableListOf<Offset>()
        interpolatedBytes.take(CAPTURE_SIZE).forEachIndexed { i, magnitude ->
            val angle = Math.toRadians((i * angleStep).toDouble())
            val barHeight = magnitude * height / 4 / 128

            val startX = (width / 2 + radius * cos(angle)).toFloat()
            val startY = (height / 2 + radius * sin(angle)).toFloat()
            val endX = (width / 2 + (radius + barHeight) * cos(angle)).toFloat()
            val endY = (height / 2 + (radius + barHeight) * sin(angle)).toFloat()

            points.add(Offset(startX, startY))
            points.add(Offset(endX, endY))
        }

        drawPoints(
            points = points,
            pointMode = PointMode.Lines,
            color = color,
            strokeWidth = 2f
        )
    }
}
