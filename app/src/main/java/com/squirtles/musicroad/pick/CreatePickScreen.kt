package com.squirtles.musicroad.pick

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.core.view.WindowInsetsControllerCompat
import coil3.compose.AsyncImage
import com.squirtles.domain.model.Song
import com.squirtles.musicroad.R
import com.squirtles.musicroad.ui.theme.Black
import com.squirtles.musicroad.ui.theme.Dark
import com.squirtles.musicroad.ui.theme.Gray
import com.squirtles.musicroad.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePickScreen(
    song: Song,
    onBackClick: () -> Unit
) {
    val comment = rememberSaveable { mutableStateOf("") }
    val scrollState = rememberScrollState()

    val dynamicBackgroundColor = Color(song.bgColor)
    val dynamicOnBackgroundColor = if (dynamicBackgroundColor.luminance() >= 0.5f) Black else White
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val windowInsetsController = WindowInsetsControllerCompat(window, view)
            val isLightStatusBar = dynamicBackgroundColor.luminance() >= 0.5f

            window.statusBarColor = dynamicBackgroundColor.toArgb()
            windowInsetsController.isAppearanceLightStatusBars = isLightStatusBar
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.pick_app_bar_title),
                        color = dynamicOnBackgroundColor,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.pick_app_bar_back_description),
                            tint = dynamicOnBackgroundColor
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = stringResource(id = R.string.pick_app_bar_upload_description),
                            tint = dynamicOnBackgroundColor
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to dynamicBackgroundColor,
                            0.47f to Black
                        )
                    )
                )
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = song.songName,
                    color = dynamicOnBackgroundColor,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                Text(
                    text = song.artistName,
                    color = dynamicOnBackgroundColor,
                    style = MaterialTheme.typography.bodyLarge
                )

                AsyncImage(
                    model = song.imageUrl,
                    contentDescription = song.albumName + stringResource(id = R.string.pick_album_description),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 30.dp)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(40.dp))

                OutlinedTextField(
                    value = comment.value,
                    onValueChange = { textValue -> comment.value = textValue },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .padding(horizontal = 30.dp),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(White),
                    placeholder = {
                        Text(
                            text = stringResource(id = R.string.pick_comment_placeholder),
                            style = MaterialTheme.typography.bodyLarge.copy(Gray)
                        )
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Dark,
                        focusedContainerColor = Dark,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    )
                )
            }
        }
    }
}

@Preview
@Composable
private fun CreatePickScreenPreview() {
    CreatePickScreen(
        song = Song(
            id = "1778132734",
            songName = "Super Shy",
            artistName = "뉴진스",
            albumName = "NewJeans 'Super Shy' - Single",
            imageUrl = "https://i.scdn.co/image/ab67616d0000b2733d98a0ae7c78a3a9babaf8af",
            genreNames = listOf("K-Pop"),
            bgColor = "#8FC1E2".toColorInt(),
            externalUrl = "",
            previewUrl = ""
        ),
        onBackClick = {}
    )
}
