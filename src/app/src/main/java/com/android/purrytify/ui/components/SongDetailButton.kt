package com.android.purrytify.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.purrytify.R

@Composable
fun SongDetailButton() {
    val isModalVisible = remember { mutableStateOf(false) }

    IconButton(onClick = { isModalVisible.value = true }) {
        Icon(
            painter = painterResource(id = R.drawable.ic_more),
            contentDescription = "More",
            tint = Color.White,
            modifier = Modifier.size(28.dp)
        )
    }

    if (isModalVisible.value) {
        CustomBottomSheet(
            isVisible = isModalVisible.value,
            onDismiss = { isModalVisible.value = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SongActionButton(
                    iconRes = R.drawable.ic_edit,
                    text = "Edit",
                    onClick = {
                        isModalVisible.value = false
                    }
                )

                Spacer(modifier = Modifier.height(4.dp))
              
                SongActionButton(
                    iconRes = R.drawable.ic_delete,
                    text = "Delete",
                    onClick = {
                        isModalVisible.value = false
                    }
                )

            }
        }
    }
}

@Composable
fun SongActionButton(iconRes: Int, text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 16.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = text,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

