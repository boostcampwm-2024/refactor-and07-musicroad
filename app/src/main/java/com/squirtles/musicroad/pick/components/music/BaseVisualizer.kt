package com.squirtles.musicroad.pick.components.music

import android.media.audiofx.Visualizer
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class BaseVisualizer(
    audioSessionId: Int
) {
    private var visualizer: Visualizer? = null

    private val _bytesFlow = MutableSharedFlow<ByteArray>(replay = 0, extraBufferCapacity = 1)
    val bytesFlow: SharedFlow<ByteArray> = _bytesFlow.asSharedFlow()

    init {
        setPlayer(audioSessionId)
    }

    private fun setPlayer(audioSessionId: Int) {
        visualizer = Visualizer(audioSessionId).apply {
            enabled = false
            captureSize = CAPTURE_SIZE
            setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
                override fun onWaveFormDataCapture(
                    visualizer: Visualizer,
                    bytes: ByteArray,
                    samplingRate: Int
                ) {
//                    Log.d("BaseVisualizer", "Received bytes: ${bytes.size}")
                    _bytesFlow.tryEmit(bytes)
                }

                override fun onFftDataCapture(
                    visualizer: Visualizer,
                    bytes: ByteArray,
                    samplingRate: Int
                ) {
                    // Not used
                }
            }, Visualizer.getMaxCaptureRate() / 2, true, false)
            enabled = true
        }
    }

    fun release() {
        visualizer?.release()
        visualizer = null
    }

    companion object {
        const val CAPTURE_SIZE = 512
    }
}
