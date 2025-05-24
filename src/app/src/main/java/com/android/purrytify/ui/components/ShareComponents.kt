package com.android.purrytify.ui.components

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.android.purrytify.R
import com.android.purrytify.data.local.entities.Song
import com.android.purrytify.utils.QRCodeUtils
import darkenColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ShareLinkButton(songId: Int) {
    val context = LocalContext.current

    val shareLink = "https://purrytify-be.vercel.app/play?songId=$songId"

    IconButton(
        onClick = {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, shareLink)
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, "Share song")
            context.startActivity(shareIntent)
        },
        modifier = Modifier.size(40.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_share),
            contentDescription = "Share Link",
            tint = Color.Unspecified,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
fun ShareQRButton(songId: Int, song: Song, dominantColor: Color = Color.Black) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showQRDialog by remember { mutableStateOf(false) }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var qrImageUri by remember { mutableStateOf<Uri?>(null) }
    
    val shareLink = "https://purrytify-be.vercel.app/play?songId=$songId"
    
    IconButton(
        onClick = {
            coroutineScope.launch {
                val bitmap = withContext(Dispatchers.Default) {
                    QRCodeUtils.generateQRCodeWithInfo(
                        shareLink,
                        song.title,
                        song.artist
                    )
                }
                qrBitmap = bitmap
                showQRDialog = true
            }
        },
        modifier = Modifier.size(40.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_qr_code),
            contentDescription = "Share QR Code",
            tint = Color.Unspecified,
            modifier = Modifier.size(32.dp)
        )
    }
    
    if (showQRDialog && qrBitmap != null) {
        QRCodeDialog(
            qrBitmap = qrBitmap!!,
            dominantColor = dominantColor,
            onDismiss = { showQRDialog = false },
            onShare = {
                coroutineScope.launch {
                    val uri = withContext(Dispatchers.IO) {
                        QRCodeUtils.saveQRCodeToStorage(
                            context,
                            qrBitmap!!,
                            "purrytify_${song.title}_qr"
                        )
                    }
                    
                    if (uri != null) {
                        qrImageUri = uri
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareLink)
                            putExtra(Intent.EXTRA_STREAM, uri)
                            type = "image/png"
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share QR Code"))
                    }
                }
            }
        )
    }
}

@Composable
fun QRCodeDialog(
    qrBitmap: Bitmap,
    dominantColor: Color,
    onDismiss: () -> Unit,
    onShare: () -> Unit
) {
    val baseColor = darkenColor(dominantColor, 0.6f)
    val darkerColor = darkenColor(dominantColor, 0.8f)
    
    val isDarkBackground = baseColor.luminance() < 0.5f
    val textColor = if (isDarkBackground) Color.White else Color.Black
    val buttonColor = if (isDarkBackground) darkenColor(baseColor, 1.2f) else darkenColor(baseColor, 0.8f)
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Share Song QR Code",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = textColor
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(darkerColor)
                    .padding(16.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .size(250.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    color = Color.White,
                    shadowElevation = 4.dp
                ) {
                    AndroidView(
                        factory = { context ->
                            android.widget.ImageView(context).apply {
                                setImageBitmap(qrBitmap)
                                scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
                                setPadding(24, 24, 24, 24)
                            }
                        }
                    )
                }
            }
        },
        containerColor = baseColor,
        titleContentColor = textColor,
        confirmButton = {
            Button(
                onClick = onShare,
                modifier = Modifier.padding(bottom = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonColor,
                    contentColor = textColor
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_share),
                        contentDescription = "Share",
                        modifier = Modifier.size(18.dp),
                        tint = textColor
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = textColor
                )
            ) {
                Text("Close")
            }
        }
    )
} 