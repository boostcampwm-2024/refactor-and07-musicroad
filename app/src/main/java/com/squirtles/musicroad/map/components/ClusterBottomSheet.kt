package com.squirtles.musicroad.map.components

import android.util.Size
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.squirtles.domain.model.LocationPoint
import com.squirtles.domain.model.Pick
import com.squirtles.domain.model.Song
import com.squirtles.musicroad.R
import com.squirtles.musicroad.create.HorizontalSpacer
import com.squirtles.musicroad.ui.theme.Gray
import com.squirtles.musicroad.ui.theme.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClusterBottomSheet(
    onDismissRequest: () -> Unit,
    clusterPickList: List<Pick>?,
    userId: String,
    calculateDistance: (Double, Double) -> String,
    onClickItem: (String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = { onDismissRequest() },
        modifier = Modifier
            .fillMaxHeight()
            .padding(WindowInsets.statusBars.asPaddingValues()),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        clusterPickList?.let { pickList ->
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        append("전체 ")
                    }
                    withStyle(
                        SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append("${pickList.size}")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = DEFAULT_PADDING),
                style = MaterialTheme.typography.titleMedium
            )
            LazyColumn {
                items(
                    items = pickList,
                    key = { it.id }
                ) { pick ->
                    BottomSheetItem(
                        song = pick.song,
                        pickLocation = pick.location,
                        createdUserName = pick.createdBy.userName.takeIf { pick.createdBy.userId != userId },
                        comment = pick.comment,
                        favoriteCount = pick.favoriteCount,
                        calculateDistance = calculateDistance,
                        onClickItem = { onClickItem(pick.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun BottomSheetItem(
    song: Song,
    pickLocation: LocationPoint,
    createdUserName: String?,
    comment: String,
    favoriteCount: Int,
    calculateDistance: (Double, Double) -> String,
    onClickItem: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClickItem() }
            .padding(horizontal = DEFAULT_PADDING, vertical = DEFAULT_PADDING / 2),
        horizontalArrangement = Arrangement.spacedBy(DEFAULT_PADDING),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(song.getImageUrlWithSize(REQUEST_IMAGE_SIZE))
                .crossfade(true)
                .build(),
            contentDescription = stringResource(R.string.map_album_image_description),
            modifier = Modifier
                .size(45.dp)
                .clip(RoundedCornerShape(4.dp)),
            placeholder = ColorPainter(Gray),
            contentScale = ContentScale.Crop,
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "${song.songName} - ${song.artistName}",
                color = MaterialTheme.colorScheme.onSurface,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.titleMedium,
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                createdUserName?.let { userName ->
                    Text(
                        text = userName,
                        modifier = Modifier.weight(weight = 1f, fill = false),
                        color = MaterialTheme.colorScheme.onSurface,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        style = MaterialTheme.typography.bodyMedium,
                    )

                    Text(
                        text = stringResource(id = R.string.map_info_window_pick_user),
                        color = MaterialTheme.colorScheme.onSurface,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                } ?: run {
                    Text(
                        text = "내가 등록한 픽",
                        modifier = Modifier.weight(weight = 1f, fill = false),
                        color = MaterialTheme.colorScheme.onSurface,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                HorizontalSpacer(8)

                Icon(
                    painter = painterResource(id = R.drawable.ic_favorite),
                    contentDescription = stringResource(R.string.map_info_window_favorite_count_icon_description),
                    tint = Primary
                )

                Text(
                    text = "$favoriteCount",
                    color = MaterialTheme.colorScheme.onSurface,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Text(
                text = comment,
                color = MaterialTheme.colorScheme.onSecondary,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Text(
            text = calculateDistance(pickLocation.latitude, pickLocation.longitude),
            modifier = Modifier.align(Alignment.Top),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private val DEFAULT_PADDING = 16.dp
private val REQUEST_IMAGE_SIZE = Size(300, 300)
