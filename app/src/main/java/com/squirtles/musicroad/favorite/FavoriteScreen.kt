package com.squirtles.musicroad.favorite

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.squirtles.domain.model.Song
import com.squirtles.musicroad.R
import com.squirtles.musicroad.common.AlbumImage
import com.squirtles.musicroad.common.CommentText
import com.squirtles.musicroad.common.Constants.DEFAULT_PADDING
import com.squirtles.musicroad.common.Constants.REQUEST_IMAGE_SIZE_DEFAULT
import com.squirtles.musicroad.common.CreatedByOtherUserText
import com.squirtles.musicroad.common.DefaultTopAppBar
import com.squirtles.musicroad.common.FavoriteCountText
import com.squirtles.musicroad.common.SongInfoText
import com.squirtles.musicroad.create.HorizontalSpacer
import com.squirtles.musicroad.ui.theme.Black
import com.squirtles.musicroad.ui.theme.Gray
import com.squirtles.musicroad.ui.theme.Primary
import com.squirtles.musicroad.ui.theme.White

@Composable
fun FavoriteScreen(
    onBackClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            DefaultTopAppBar(
                title = stringResource(R.string.favorite_top_app_bar_title),
                onBackClick = onBackClick
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(colorStops = colorStops))
                .padding(innerPadding)
        ) {
            LazyColumn {

            }
        }
    }
}

@Composable
fun PickItem(
    song: Song,
    createUserName: String,
    favoriteCount: Int,
    comment: String,
    onClickItem: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = White),
            ) { onClickItem() }
            .padding(horizontal = DEFAULT_PADDING, vertical = DEFAULT_PADDING / 2),
        horizontalArrangement = Arrangement.spacedBy(DEFAULT_PADDING),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AlbumImage(
            imageUrl = song.getImageUrlWithSize(REQUEST_IMAGE_SIZE_DEFAULT),
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(8.dp))
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            SongInfoText(
                songInfo = "${song.songName} - ${song.artistName}",
                color = White
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                CreatedByOtherUserText(
                    userName = createUserName,
                    modifier = Modifier.weight(weight = 1f, fill = false),
                    color = Gray
                )

                HorizontalSpacer(8)

                FavoriteCountText(
                    favoriteCount = favoriteCount,
                    iconTint = Gray,
                    color = Gray
                )
            }

            CommentText(
                comment = comment,
                color = Gray
            )
        }
    }
}

private val colorStops = arrayOf(
    0.0f to Primary,
    0.25f to Black
)
