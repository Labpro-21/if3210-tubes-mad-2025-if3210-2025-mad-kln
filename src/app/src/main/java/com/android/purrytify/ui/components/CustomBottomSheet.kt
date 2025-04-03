package com.android.purrytify.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomBottomSheet(
    isVisible: Boolean,
    onDismiss: (refresh: Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    if (isVisible) {
        ModalBottomSheet(
            onDismissRequest = { onDismiss(false) },
            sheetState = sheetState,
            dragHandle = null,
            containerColor = Color(0xFF212121)
        ) {
            CustomHandle()
            content()
        }
    }
}

@Composable
fun CustomHandle() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 24.dp)
            .background(Color(0xFF212121)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(50.dp)
                .height(5.dp)
                .background(Color.Gray, shape = RoundedCornerShape(50))
        )
    }
}