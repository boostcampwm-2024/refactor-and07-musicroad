package com.squirtles.musicroad

import android.os.Bundle
import android.view.View
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import com.squirtles.musicroad.map.MapFragment
import com.squirtles.musicroad.ui.theme.MusicRoadTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MusicRoadTheme {
                MapFragmentContainer()
            }
        }
    }
}

@Composable
fun MapFragmentContainer() {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            FragmentContainerView(context).apply {
                id = View.generateViewId()

                (context as FragmentActivity).supportFragmentManager.beginTransaction()
                    .replace(id, MapFragment())
                    .commitAllowingStateLoss()
            }
        }
    )
}