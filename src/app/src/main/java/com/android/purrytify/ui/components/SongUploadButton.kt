package com.android.purrytify.ui.components

import android.util.Log
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.purrytify.ui.modal.SongUploadModal

@Composable
fun SongUploadButton(func: () -> Unit) {

    val isModalVisible = remember { mutableStateOf(false) }
    Button(
        onClick = { isModalVisible.value = true },
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        modifier = Modifier
            .padding(0.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = "+",
            color = Color.White,
            fontSize = 40.sp,
            fontWeight = FontWeight.Light,
        )
    }

    SongUploadModal(
        isVisible = isModalVisible.value,
        onDismiss = { refresh ->
            isModalVisible.value = false
            if (refresh) {
                Log.d("Log", "Modal closed: Refreshing songs")
                func()
            } else {
                Log.d("Log", "Modal closed: Not refreshing songs")
            }
        },
    )
}
