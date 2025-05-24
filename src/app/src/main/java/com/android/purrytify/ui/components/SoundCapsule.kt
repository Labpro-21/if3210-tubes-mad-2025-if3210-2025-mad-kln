package com.android.purrytify.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.android.purrytify.R
import com.android.purrytify.view_model.TopArtistInfo
import com.android.purrytify.view_model.TopSongInfo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TimeListenedCard(
    timeListened: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .height(100.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF212121)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Text(
                    text = "Time listened",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = timeListened,
                    color = Color(0xFF1DB954),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Details",
                tint = Color.White,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    }
}

@Composable
fun TimeListenedDetailModal(onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .clickable(onClick = { onDismiss() }),
        contentAlignment = Alignment.TopStart
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier
                        .clickable { onDismiss() }
                        .padding(end = 16.dp)
                        .size(24.dp)
                )
                
                Text(
                    text = "Time Listened",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Time Listened Detail Page",
                color = Color.Gray,
                fontSize = 16.sp,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp)
            )
        }
    }
}

@Composable
fun TopArtistCard(
    title: String,
    imageUri: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .width(160.dp) 
            .height(200.dp) 
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF212121)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Top artist",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Details",
                    tint = Color.White,
                )
            }
            Text(
                text = title,
                color = Color(0xFF669BEC),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(bottom = 10.dp)
            )
            Image(
                painter = rememberAsyncImagePainter(model = imageUri.ifEmpty { null }),
                contentDescription = title,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.Gray),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun TopArtistDetailModal(
    onDismiss: () -> Unit, 
    topArtists: List<TopArtistInfo>, 
    artistCount: Int
) {
    val currentMonthYear = remember {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)) 
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier
                        .clickable { onDismiss() }
                        .padding(end = 16.dp)
                        .size(24.dp)
                )
                Text(
                    text = "Top artists",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = currentMonthYear,
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = buildAnnotatedString {
                        append("You listened to ")
                        withStyle(style = SpanStyle(color = Color(0xFF669BEC), fontWeight = FontWeight.Bold)) {
                            append("$artistCount")
                        }
                        append(" artists this month.")
                    },
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            LazyColumn(modifier = Modifier.padding(horizontal = 8.dp)) {
                items(topArtists) { artistInfo ->
                    TopArtistItemCard(
                        rank = artistInfo.rank,
                        artistName = artistInfo.artist,
                        imageUri = artistInfo.imageUri,
                    )
                    HorizontalDivider(color = Color.DarkGray, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 8.dp))
                }
            }
        }
    }
}

@Composable
fun TopSongCard(
    title: String,
    imageUri: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .width(160.dp) 
            .height(200.dp) 
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF212121)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Top song",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Details",
                    tint = Color.White,
                )
            }
            Text(
                text = title,
                color = Color(0xFFF8E747),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(bottom = 10.dp)
            )
            Image(
                painter = rememberAsyncImagePainter(model = imageUri.ifEmpty { null }),
                contentDescription = title,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.Gray),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun TopSongDetailModal(
    onDismiss: () -> Unit,
    topSongs: List<TopSongInfo>,
    songCount: Int
) {
    val currentMonthYear = remember {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier
                        .clickable { onDismiss() }
                        .padding(end = 16.dp)
                        .size(24.dp)
                )
                Text(
                    text = "Top songs",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = currentMonthYear,
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = buildAnnotatedString {
                        append("You played ")
                        withStyle(style = SpanStyle(color = Color(0xFFF8E747), fontWeight = FontWeight.Bold)) { // Yellow for song count
                            append("$songCount")
                        }
                        append(" different songs this month.")
                    },
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            LazyColumn(modifier = Modifier.padding(horizontal = 8.dp)) {
                items(topSongs) { songInfo ->
                    TopSongItemCard(
                        songInfo = songInfo,
                    )
                    HorizontalDivider(color = Color.DarkGray, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 8.dp))
                }
            }
        }
    }
}

@Composable
fun MaxStreakCard(
    songTitle: String,
    artistName: String,
    streakCount: Int,
    imageUri: String,
    dateRange: String,
){
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF212121)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = imageUri.ifEmpty { null }),
                contentDescription = "$songTitle by $artistName",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(16.dp))
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "You had a $streakCount-day streak",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = buildAnnotatedString {
                        append("You played ")
                        withStyle(style = SpanStyle(color = Color(0xFF9EC4CC))) {
                            append(songTitle)
                        }
                        append(" by ")
                        withStyle(style = SpanStyle(color = Color(0xFF9EC4CC))) {
                            append(artistName)
                        }
                        append(" day after day. You were on fire")
                    },
                    color = Color.Gray,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dateRange,
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = "Share Streak",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TopArtistItemCard(
    rank: Int,
    artistName: String,
    imageUri: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = String.format("%02d", rank),
            color = Color(0xFF669BEC),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(30.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = artistName,
            color = Color.White,
            fontSize = 16.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Image(
            painter = rememberAsyncImagePainter(model = imageUri.ifEmpty { null }),
            contentDescription = artistName,
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color.DarkGray),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun TopSongItemCard(
    songInfo: TopSongInfo,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = String.format("%02d", songInfo.rank),
            color = Color(0xFFF8E747),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(30.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .height(80.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = songInfo.title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = songInfo.artist,
                    color = Color.Gray,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = "${songInfo.playCount} plays",
                color = Color.LightGray,
                fontSize = 10.sp
            )
        }

        Spacer(modifier = Modifier.width(16.dp))
        Image(
            painter = rememberAsyncImagePainter(model = songInfo.imageUri.ifEmpty { null }),
            contentDescription = songInfo.title,
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(8.dp)) 
                .background(Color.DarkGray),
            contentScale = ContentScale.Crop
        )
    }
}
