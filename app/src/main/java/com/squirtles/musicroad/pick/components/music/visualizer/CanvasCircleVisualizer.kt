package com.squirtles.musicroad.pick.components.music.visualizer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

@Composable
internal fun CanvasCircle(
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
internal fun CanvasSoundEffect(
    audioData: List<Float>,
    color: Color,
    modifier: Modifier = Modifier
) {

    val offsetAngle = Math.toRadians(-90.0)
    val angleStep = 360f / audioData.size
    val points = mutableListOf<Offset>()

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val radius = max(width, height) * 0.45f

        audioData.forEachIndexed { i, magnitude ->
            val angle = Math.toRadians((i * angleStep).toDouble()) + offsetAngle
            val cosValue = cos(angle)
            val sinValue = sin(angle)
            val barHeight = magnitude * height / 4

            val startX = (width / 2 + radius * cosValue).toFloat()
            val startY = (height / 2 + radius * sinValue).toFloat()
            val endX = (width / 2 + (radius + barHeight) * cosValue).toFloat()
            val endY = (height / 2 + (radius + barHeight) * sinValue).toFloat()

            points.add(Offset(startX, startY))
            points.add(Offset(endX, endY))
        }

        drawPoints(
            points = points,
            pointMode = PointMode.Lines,
            color = color,
            strokeWidth = 12f
        )
    }
}
