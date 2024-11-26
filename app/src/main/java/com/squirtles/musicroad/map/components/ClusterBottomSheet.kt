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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClusterBottomSheet(
    onDismissRequest: () -> Unit,
    clusterPickList: List<Pick>?,
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
            LazyColumn {
                items(
                    items = pickList,
                    key = { it.id }
                ) { pick ->
                    BottomSheetItem(
                        song = pick.song,
                        pickLocation = pick.location,
                        // TODO: createdBy
                        // TODO: comment
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
    calculateDistance: (Double, Double) -> String,
    onClickItem: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClickItem() }
            .padding(horizontal = DEFAULT_PADDING, vertical = DEFAULT_PADDING / 2)
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

        HorizontalSpacer(16)

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${song.songName} - ${song.artistName}",
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = DEFAULT_PADDING),
                    color = MaterialTheme.colorScheme.onSurface,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = calculateDistance(pickLocation.latitude, pickLocation.longitude),
                    style = MaterialTheme.typography.bodyMedium.copy(Gray)
                )
            }

            // TODO: 나머지 정보들: 누가 등록한 픽인지, 한마디
        }
    }
}

private val DEFAULT_PADDING = 16.dp
private val REQUEST_IMAGE_SIZE = Size(300, 300)
