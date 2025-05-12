package com.android.purrytify.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

fun colorGradientFromString(color: String): List<Color> {
    return when (color.lowercase()) {
        "red" -> listOf(
            Color(0xFFE87A7A),
            Color(0xFFD80832)
        )
        "blue" -> listOf(
            Color(0xCB47C1AA),
            Color(0xFF0C2E6E)
        )
        "green" -> listOf(
            Color(0xFF69F0AE),
            Color(0xFF0B9843)
        )
        else -> listOf(
            Color(0xFF78909C),
            Color(0xFF455A64)
        )
    }
}

@Composable
fun ChartCard(
    color: String,
    title: String,
    subtitle: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gradientColors = colorGradientFromString(color)

    Column(
        modifier = modifier
            .width(120.dp)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(
                    brush = Brush.verticalGradient(gradientColors)
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                HorizontalDivider(
                    modifier = Modifier
                        .width(60.dp)
                        .padding(vertical = 7.dp),
                    color = Color.White.copy(alpha = 0.5f),
                    thickness = 1.dp
                )
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
            }
        }
        Text(
            text = description,
            color = Color.LightGray,
            fontSize = 12.sp,
            modifier = Modifier
                .padding(top = 8.dp)
                .align(Alignment.CenterHorizontally),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}