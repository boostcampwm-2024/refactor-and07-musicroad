package com.squirtles.musicroad.pick.components.music.visualizer

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch

@Composable
fun CircleVisualizer(
    audioSessionId: Int,
    color: Color = Color.White,
    modifier: Modifier = Modifier
) {
    val baseVisualizer = remember { BaseVisualizer(audioSessionId) }
//    val magnitudes = remember { mutableStateOf<List<Float>>(emptyList()) }
//    LaunchedEffect(Unit) {
//        baseVisualizer.fftFlow.collect { fftMagnitudes ->
//            val newMagnitudes = if (magnitudes.value.isEmpty()) {
//                fftMagnitudes
//            } else {
//                magnitudes.value.mapIndexed { i, oldMagnitude ->
//                    lerp(
//                        start = oldMagnitude,
//                        stop = fftMagnitudes[i],
//                        fraction = 0.4f
//                    )
//                }
//            }
//
//            magnitudes.value = processAudioData(newMagnitudes)
////            Log.d("CircleVisualizer", "Received FFT data: ${magnitudes.value.joinToString(",")}")
//        }
//    }
    val magnitudes = remember { mutableStateOf<List<Animatable<Float, AnimationVector1D>>>(emptyList()) }
    LaunchedEffect(Unit) {
        baseVisualizer.fftFlow.collect { fftArray ->
            val normalizedData = processAudioData(fftArray)
            if (magnitudes.value.isEmpty()) {
                magnitudes.value = normalizedData.map { Animatable(it) }
            } else {
                normalizedData.forEachIndexed { i, magnitude ->
                    launch {
                        magnitudes.value[i].animateTo(
                            targetValue = magnitude,
                            animationSpec = tween(
                                durationMillis = 150,
                                easing = FastOutSlowInEasing
                            )
                        )
                    }
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            baseVisualizer.release()
        }
    }
    
    CanvasSoundEffect(
        audioData = magnitudes.value.map { it.value },
        color = color,
        modifier = modifier
    )
}

private fun processAudioData(audioData: List<Float>): List<Float> {
    val scaledData = scaleAudioData(audioData)
    return normalizeAudioData(scaledData)
}

/* 주파수 대역별 가중치 */
private fun scaleAudioData(audioData: List<Float>): List<Float> {
    val size = audioData.size
    return audioData.mapIndexed { index, value ->
        val scaleFactor = if (index < size / 8) 0.5f
        else if (index < size / 4) 1.0f // 저주파 대역
        else if (index < size / 2) 1.2f // 중간 대역
//        else if (index < size / 1.5) 1.5f // 고주파 대역
        else 1.5f // 고주파 대역
        value * scaleFactor
    }
}

/* 0.0 ~ 1.0 사이 정규화  */
private fun normalizeAudioData(audioData: List<Float>): List<Float> {
    val max = audioData.maxOrNull() ?: 1f // 데이터 최대값
    val min = audioData.minOrNull() ?: 0f // 데이터 최소값
    return if (max - min <= 1f) List(audioData.size) { 0f } else audioData.map { (it - min) / (max - min) }
}
