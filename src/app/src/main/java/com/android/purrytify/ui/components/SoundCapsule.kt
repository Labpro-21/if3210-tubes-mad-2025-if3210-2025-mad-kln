package com.android.purrytify.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter

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
                modifier = Modifier.padding(bottom = 10.dp)
            )
            Image(
                painter = rememberAsyncImagePainter(model = imageUri),
                contentDescription = title,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun TopArtistDetailModal(onDismiss: () -> Unit, title: String, imageUri: String) {
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
                    text = "Top Artist",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                 Text(
                    text = "Top Artist Detail Page",
                    color = Color.Gray,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
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
                modifier = Modifier.padding(bottom = 10.dp)
            )
            Image(
                painter = rememberAsyncImagePainter(model = imageUri),
                contentDescription = title,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun TopSongDetailModal(onDismiss: () -> Unit, title: String, imageUri: String) {
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
                    text = "Top Song",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                 Text(
                    text = "Top Song Detail Page",
                    color = Color.Gray,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
