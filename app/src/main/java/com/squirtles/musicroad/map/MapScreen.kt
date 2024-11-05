package com.squirtles.musicroad.map

import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import com.squirtles.musicroad.R
import com.squirtles.musicroad.ui.theme.Primary
import com.squirtles.musicroad.ui.theme.White

@Composable
fun MapScreen() {
    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
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

            BottomNavigation(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            )
        }
    }
}

@Composable
fun BottomNavigation(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .size(245.dp, 50.dp)
                .background(color = Color.White, shape = CircleShape)
        ) {
            IconButton(
                onClick = { /* TODO: 픽 보관함 이동 */ },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = "favorite icon",
                        modifier = Modifier.padding(start = BottomNavigationHorizontalPadding),
                        tint = Primary
                    )
                }
            }

            IconButton(
                onClick = { /* TODO: 설정 이동 */ },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AccountCircle,
                        contentDescription = "Person Icon",
                        modifier = Modifier.padding(end = BottomNavigationHorizontalPadding),
                        tint = Primary
                    )
                }
            }
        }

        // 중앙 버튼
        Box(
            modifier = Modifier.size(82.dp),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = { /* TODO: 픽 등록 이동 */ },
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Primary, shape = CircleShape)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_musical_note_64),
                    contentDescription = "픽 등록하기 버튼 아이콘",
                    modifier = Modifier.size(34.dp),
                    tint = White
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BottomNavigationPreview() {
    BottomNavigation()
}

private val BottomNavigationHorizontalPadding = 32.dp