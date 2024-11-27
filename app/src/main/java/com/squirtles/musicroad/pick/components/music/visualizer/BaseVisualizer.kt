package com.squirtles.musicroad.pick.components.music.visualizer

import android.media.audiofx.Visualizer
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlin.math.sqrt

class BaseVisualizer(
    audioSessionId: Int
) {
    private var visualizer: Visualizer? = null

    private val _bytesFlow = MutableSharedFlow<ByteArray>(replay = 0, extraBufferCapacity = 1)
    val bytesFlow: SharedFlow<ByteArray> = _bytesFlow.asSharedFlow()

    private val _fftFlow = MutableSharedFlow<List<Float>>(replay = 0, extraBufferCapacity = 1)
    val fftFlow: SharedFlow<List<Float>> = _fftFlow.asSharedFlow()

    private val validRange = getSignificantFftIndexRange()

    init {
        setPlayer(audioSessionId)
    }

    private fun setPlayer(audioSessionId: Int) {
        visualizer = Visualizer(audioSessionId).apply {
            enabled = false
            captureSize = CAPTURE_SIZE
            setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
                override fun onWaveFormDataCapture(visualizer: Visualizer, bytes: ByteArray, samplingRate: Int) {
                    // NOT USED
                }

                override fun onFftDataCapture(visualizer: Visualizer, bytes: ByteArray, samplingRate: Int) {
                    // bytes = [실수부,허수부,실수부,허수부 ....]
                    val magnitudes = mutableListOf<Float>()
                    for (i in 0 until bytes.size / 2 step 2) {
                        // 복소수
                        val real = bytes[i].toInt()
                        val imaginary = bytes[i + 1].toInt()

                        val magnitude = sqrt((real * real + imaginary * imaginary).toDouble()).toFloat()
                        magnitudes.add(magnitude)
                    }
                    val filteredMagnitudes = magnitudes.slice(validRange)
//                    Log.d("BaseVisualizer", "filtered fft data ${filteredMagnitudes.size}: ${filteredMagnitudes.joinToString(",")}")
                    _fftFlow.tryEmit(filteredMagnitudes)
                }
            }, Visualizer.getMaxCaptureRate() / 2, false, true)
            enabled = true
        }
    }

    fun release() {
        visualizer?.release()
        visualizer = null
    }

    // 20 ~ 4000 Hz 사이만 필터링
    private fun getSignificantFftIndexRange(
        samplingRate: Int = SAMPLING_RATE,
        captureSize: Int = CAPTURE_SIZE,
        minFreq: Int = MIN_FREQ,
        maxFreq: Int = MAX_FREQ
    ): IntRange {
        val resolution = samplingRate.toDouble() / captureSize
        val startIndex = (minFreq / resolution).toInt()
        val endIndex = (maxFreq / resolution).toInt()
        return startIndex..endIndex
    }

    companion object {
        const val CAPTURE_SIZE = 1024
        const val SAMPLING_RATE = 48000
        const val MIN_FREQ = 20
        const val MAX_FREQ = 4000
    }
}
