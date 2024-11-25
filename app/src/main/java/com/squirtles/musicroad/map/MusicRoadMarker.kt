package com.squirtles.musicroad.map

import android.content.Context
import android.util.Size
import androidx.compose.ui.graphics.toArgb
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.squirtles.domain.model.Pick
import com.squirtles.musicroad.map.components.MarkerIconView
import com.squirtles.musicroad.ui.theme.Blue

class MusicRoadMarker(
    val userId: String = "",
    val pick: Pick,
) {
    val position get() = marker.position
    val map get() = marker.map
    private val marker = Marker()

    operator fun invoke() = marker

    fun clearMap() {
        marker.map = null
    }

    fun loadMarkerImage(context: Context, map: NaverMap, onMarkerClick: () -> Unit) {
        val markerIconView = MarkerIconView(context).apply {
            setPaintColor(Blue.toArgb())
        }

        markerIconView.loadImage(pick.song.getImageUrlWithSize(REQUEST_IMAGE_SIZE)) {
            marker.position = LatLng(pick.location.latitude, pick.location.longitude)
            marker.icon = OverlayImage.fromView(markerIconView)
            marker.setOnClickListener {
                onMarkerClick()
                true
            }
            marker.map = map
        }
    }

    fun toggleSizeByClick(context: Context, isClicked: Boolean) {
        val defaultIconWidth = marker.icon.getIntrinsicWidth(context)
        val defaultIconHeight = marker.icon.getIntrinsicHeight(context)

        marker.width =
            if (isClicked) (defaultIconWidth * MARKER_SCALE).toInt() else defaultIconWidth
        marker.height =
            if (isClicked) (defaultIconHeight * MARKER_SCALE).toInt() else defaultIconHeight
    }

    companion object {
        val REQUEST_IMAGE_SIZE = Size(300, 300)
        const val MARKER_SCALE = 1.5
    }
}
