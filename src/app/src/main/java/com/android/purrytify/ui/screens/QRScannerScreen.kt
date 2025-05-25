package com.android.purrytify.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHostController
import com.android.purrytify.SharedSongState
import com.android.purrytify.data.local.entities.Song
import com.android.purrytify.network.RetrofitClient
import com.android.purrytify.utils.QRCodeUtils
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.CompoundBarcodeView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.regex.Pattern
import android.content.pm.ActivityInfo

@Composable
fun QRScannerScreen(navController: NavHostController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
            if (!granted) {
                Toast.makeText(
                    context,
                    "Camera permission is required to scan QR codes",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    )
    
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    
    if (hasCameraPermission) {
        var captureManager: CaptureManager? = null
        val barcodeView = remember {
            CompoundBarcodeView(context).apply {
                val callback = object : BarcodeCallback {
                    override fun barcodeResult(result: BarcodeResult?) {
                        result?.let { barcodeResult ->
                            val content = barcodeResult.text
                            
                            if (QRCodeUtils.isValidPurrytifyQRCode(content)) {
                                val pattern = Pattern.compile("songId=(\\d+)")
                                val matcher = pattern.matcher(content)
                                
                                if (matcher.find()) {
                                    val songId = matcher.group(1)?.toIntOrNull()
                                    
                                    if (songId != null) {
                                        pauseAndWait()
                                        
                                        CoroutineScope(Dispatchers.Main).launch {
                                            try {
                                                val response = withContext(Dispatchers.IO) {
                                                    RetrofitClient.api.getSongById(songId)
                                                }
                                                
                                                val song = Song(
                                                    id = response.id,
                                                    title = response.title,
                                                    artist = response.artist,
                                                    audioUri = response.url,
                                                    imageUri = response.artwork
                                                )
                                                
                                                SharedSongState.setSong(song)
                                                navController.navigate("home") {
                                                    popUpTo("home") { inclusive = true }
                                                }
                                                
                                            } catch (e: Exception) {
                                                Log.e("QRScannerScreen", "Error processing QR code", e)
                                                Toast.makeText(
                                                    context,
                                                    "Failed to load song. Please try again.",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                
                                                resume()
                                            }
                                        }
                                    }
                                }
                            } else {
                                Toast.makeText(
                                    context,
                                    "Invalid QR code. Please scan a Purrytify song QR code.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
                
                decodeContinuous(callback)
            }
        }
        
        DisposableEffect(lifecycleOwner) {
            captureManager = CaptureManager(context as androidx.activity.ComponentActivity, barcodeView)
            captureManager?.initializeFromIntent(context.intent, null)
            
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_RESUME -> {
                        captureManager?.onResume()
                        barcodeView.resume()
                    }
                    Lifecycle.Event.ON_PAUSE -> {
                        captureManager?.onPause()
                        barcodeView.pause()
                    }
                    Lifecycle.Event.ON_DESTROY -> {
                        captureManager?.onDestroy()
                    }
                    else -> {}
                }
            }
            
            lifecycleOwner.lifecycle.addObserver(observer)
            
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
                captureManager?.onDestroy()
                val activity = context as? androidx.activity.ComponentActivity
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }
        
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                factory = { barcodeView },
                modifier = Modifier.fillMaxSize()
            )
            
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))
                Text("Scan Purrytify QR Code")
            }
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Camera permission is required to scan QR codes")
        }
    }
} 