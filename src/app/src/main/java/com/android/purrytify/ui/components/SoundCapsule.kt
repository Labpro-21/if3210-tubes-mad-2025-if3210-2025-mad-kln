package com.android.purrytify.ui.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.android.purrytify.R
import com.android.purrytify.view_model.DailyPlayback
import com.android.purrytify.view_model.TopArtistInfo
import com.android.purrytify.view_model.TopSongInfo
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

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
fun TimeListenedDetailModal(
    onDismiss: () -> Unit,
    currentMonthYear: String,
    totalMinutes: Int,
    dailyAverageMinutes: Int,
    dailyPlayback: List<DailyPlayback>
) {
    val density = LocalDensity.current
    val textPaint = remember {
        android.graphics.Paint().apply {
            color = Color.Gray.toArgb()
            textSize = with(density) { 10.sp.toPx() }
        }
    }
    val yAxisLabelPaint = remember {
        android.graphics.Paint().apply {
            color = Color.Gray.toArgb()
            textSize = with(density) { 10.sp.toPx() }
            textAlign = android.graphics.Paint.Align.RIGHT
        }
    }
    val xAxisLabelPaint = remember {
        android.graphics.Paint().apply {
            color = Color.Gray.toArgb()
            textSize = with(density) { 10.sp.toPx() }
            textAlign = android.graphics.Paint.Align.CENTER
        }
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
                    text = "Time listened",
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
                        append("You listened to music for ")
                        withStyle(style = SpanStyle(color = Color(0xFF1DB954), fontWeight = FontWeight.Bold)) {
                            append("$totalMinutes minutes")
                        }
                        append(" this month.")
                    },
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Daily average: $dailyAverageMinutes min",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .background(Color(0xFF1A1A1A), shape = RoundedCornerShape(8.dp)),
            ) {
                if (dailyPlayback.isNotEmpty()) {
                    Canvas(modifier = Modifier.fillMaxSize().padding(start = 8.dp, end = 8.dp, top = 16.dp, bottom = 16.dp)) {
                        val yAxisLabelOffset = 8.dp.toPx()
                        val bottomPaddingForXAxisLabels = 30.dp.toPx()
                        val leftPaddingForYAxisLabels = 40.dp.toPx()
                        
                        val chartHeight = size.height - bottomPaddingForXAxisLabels
                        val chartWidth = size.width - leftPaddingForYAxisLabels

                        val maxMinutesRaw = dailyPlayback.maxOfOrNull { it.minutesListened } ?: 0
                        val maxMinutes = if (maxMinutesRaw == 0) 1 else maxMinutesRaw
                        
                        val calendar = Calendar.getInstance()
                        try {
                            val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                            val date = sdf.parse(currentMonthYear)
                            if (date != null) calendar.time = date
                        } catch (e: Exception) { }
                        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

                        drawLine(
                            color = Color.Gray,
                            start = Offset(leftPaddingForYAxisLabels, 0f),
                            end = Offset(leftPaddingForYAxisLabels, chartHeight),
                            strokeWidth = 1.dp.toPx()
                        )
                        drawContext.canvas.nativeCanvas.save()
                        drawContext.canvas.nativeCanvas.rotate(-90f, yAxisLabelOffset , chartHeight / 2)
                        drawContext.canvas.nativeCanvas.drawText(
                            "minutes",
                            yAxisLabelOffset,
                            chartHeight / 2  + textPaint.textSize / 3,
                            textPaint.apply { textAlign = android.graphics.Paint.Align.CENTER}
                        )
                        drawContext.canvas.nativeCanvas.restore()

                        val yLabelCount = 3
                        for (i in 0 until yLabelCount) {
                            val value = (maxMinutes.toFloat() / (yLabelCount - 1)) * i
                            val yPos = chartHeight - (value / maxMinutes) * chartHeight
                            val labelText = value.roundToInt().toString()
                            drawContext.canvas.nativeCanvas.drawText(
                                labelText,
                                leftPaddingForYAxisLabels - yAxisLabelOffset,
                                yPos + textPaint.textSize / 3,
                                yAxisLabelPaint
                            )
                        }

                        drawLine(
                            color = Color.Gray,
                            start = Offset(leftPaddingForYAxisLabels, chartHeight),
                            end = Offset(size.width, chartHeight),
                            strokeWidth = 1.dp.toPx()
                        )
                        drawContext.canvas.nativeCanvas.drawText(
                            "day",
                            leftPaddingForYAxisLabels + chartWidth / 2,
                            size.height - (bottomPaddingForXAxisLabels / 8) + 10.dp.toPx(),
                            textPaint.apply { textAlign = android.graphics.Paint.Align.CENTER }
                        )

                        val xLabelPoints = listOf(1, (daysInMonth + 1) / 2, daysInMonth)
                        xLabelPoints.distinct().forEach { day ->
                             val xPos = leftPaddingForYAxisLabels + (day.toFloat() / daysInMonth) * chartWidth
                             drawContext.canvas.nativeCanvas.drawText(
                                day.toString(),
                                xPos,
                                chartHeight + bottomPaddingForXAxisLabels * 0.75f,
                                xAxisLabelPaint
                            )
                        }

                        val linePath = Path()
                        dailyPlayback.sortedBy { it.dayOfMonth }.forEachIndexed { index, playback ->
                            val x = leftPaddingForYAxisLabels + (playback.dayOfMonth.toFloat() / daysInMonth) * chartWidth
                            val y = chartHeight - (playback.minutesListened.toFloat() / max(maxMinutes,1)) * chartHeight
                            
                            if (index == 0) {
                                linePath.moveTo(x, y)
                            } else {
                                linePath.lineTo(x, y)
                            }
                        }
                        drawPath(
                            path = linePath,
                            color = Color(0xFF1DB954),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                } else {
                     Text(
                        text = "No listening data for this month",
                        color = Color.Gray,
                        fontSize = 16.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
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
                        withStyle(style = SpanStyle(color = Color(0xFFF8E747), fontWeight = FontWeight.Bold)) { 
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
