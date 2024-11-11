package com.squirtles.musicroad.pick

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.squirtles.musicroad.R
import com.squirtles.musicroad.ui.theme.Black
import com.squirtles.musicroad.ui.theme.Dark
import com.squirtles.musicroad.ui.theme.Gray
import com.squirtles.musicroad.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePickScreen(
    name: String,
    songTitle: String,
    artists: List<String>,
    album: String,
    imageUrl: String
) {
    val dynamicOnBackgroundColor = White // todo 배경에 따라 텍스트 색상 설정 필요
    val dynamicBackgroundColor = Black // todo 배경색 설정
    val comment = rememberSaveable { mutableStateOf("") }
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = name + stringResource(id = R.string.pick_app_bar_title_user),
                        color = dynamicOnBackgroundColor
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { /*TODO*/ }) {
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
        },
        containerColor = dynamicBackgroundColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = songTitle,
                color = dynamicOnBackgroundColor,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            Text(
                text = artists.joinToString(),
                color = dynamicOnBackgroundColor,
                style = MaterialTheme.typography.bodyLarge
            )

            AsyncImage(
                model = imageUrl,
                contentDescription = album + stringResource(id = R.string.pick_album_description),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp)
                    .aspectRatio(1.1f)
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

@Preview
@Composable
private fun CreatePickScreenPreview() {
    CreatePickScreen(
        name = "짱구",
        songTitle = "Ditto",
        artists = listOf("NewJeans"),
        album = "",
        imageUrl = "https://i.scdn.co/image/ab67616d0000b2733d98a0ae7c78a3a9babaf8af"
    )
}