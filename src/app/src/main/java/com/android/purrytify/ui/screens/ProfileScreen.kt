package com.android.purrytify.ui.screens


import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.android.purrytify.R
import com.android.purrytify.data.local.RepositoryProvider
import com.android.purrytify.datastore.TokenManager
import com.android.purrytify.network.RetrofitClient
import com.android.purrytify.network.RetrofitClient.api
import com.android.purrytify.network.checkToken
import com.android.purrytify.network.prepareMultipart
import com.android.purrytify.ui.components.MapPickerActivity
import com.android.purrytify.ui.components.TopArtistCard
import com.android.purrytify.ui.components.TopArtistDetailModal
import com.android.purrytify.ui.components.TopSongCard
import com.android.purrytify.ui.components.TopSongDetailModal
import com.android.purrytify.ui.components.TimeListenedCard
import com.android.purrytify.ui.components.TimeListenedDetailModal
import com.android.purrytify.ui.components.MaxStreakCard
import com.android.purrytify.view_model.PlayerViewModel
import com.android.purrytify.view_model.SoundCapsuleViewModel
import com.android.purrytify.view_model.getPlayerViewModel
import com.android.purrytify.view_model.getSoundCapsuleViewModel
import com.google.android.gms.location.LocationServices
import extractDominantColor
import getCountryNameFromCode
import getToken
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import loadBitmapFromUrl
import java.io.File
import java.io.IOException
import java.util.Locale


@Composable
fun ProfileScreen(
    navController: NavController,
    mediaPlayerViewModel: PlayerViewModel = getPlayerViewModel(),
    soundCapsuleViewModel: SoundCapsuleViewModel = getSoundCapsuleViewModel(),
) {
    val userRepository = RepositoryProvider.getUserRepository()
    val songRepository = RepositoryProvider.getSongRepository()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var dominantColor by remember { mutableStateOf(Color.Black) }

    // Profile state
    var editProfile by remember { mutableStateOf(false) }
    var editLocation by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("USER") }
    var country by remember { mutableStateOf("") }
    var locationCode by remember { mutableStateOf("") }
    var profileURL by remember { mutableStateOf("") }
    var photoProfileFile by remember { mutableStateOf<File?>(null) }
    var uploadPhoto by remember { mutableStateOf(false) }


    // Stats state
    var songCountStats by remember { mutableIntStateOf(0) }
    var likeCount by remember { mutableIntStateOf(0) }
    var listenedCount by remember { mutableIntStateOf(0) }
    
    // Modal states
    var showTimeListenedModal by remember { mutableStateOf(false) }
    var showTopArtistModal by remember { mutableStateOf(false) }
    var showTopSongModal by remember { mutableStateOf(false) }



    suspend fun fetchUserId(context: Context): Int {
        Log.d("DEBUG_PROFILE", "Fetching user ID")
        return TokenManager.getCurrentId(context).firstOrNull()!!
    }

    fun useCurrentLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val countryCode = getCountryCodeFromLocation(
                    context,
                    location.latitude,
                    location.longitude
                )
                Log.d("Location", "Country Code: $countryCode")
                country = getCountryNameFromCode(countryCode!!)
                locationCode = countryCode
            } else {
                Log.e("Location", "Location is null")
            }
        }.addOnFailureListener {
            Log.e("Location", "Failed to get location: ${it.message}")
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            useCurrentLocation()
        } else {
            Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    val mapLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val lat = result.data?.getDoubleExtra("latitude", 0.0)
            val lng = result.data?.getDoubleExtra("longitude", 0.0)
            if (lat != null && lng != null) {
                val countryCode = getCountryCodeFromLocation(context, lat, lng)
                Log.d("Location", "Country Code: $countryCode")
                locationCode = countryCode!!
                country = getCountryNameFromCode(locationCode)
            }
        }
    }

    fun editProfile(context:Context, countryParam : String?, photo : File?){
        val (locationValue, imageValue) = prepareMultipart(countryParam, photo)
        coroutineScope.launch {
            try {
                val token = getToken(context)
                val bearerToken = "Bearer $token"
                api.editProfile(
                    token = bearerToken,
                    location = locationValue,
                    profilePhoto = imageValue
                )
                Log.d("Profile", "Update successful")
            } catch (e: Exception) {
                Log.e("Profile", "Update failed: ${e.message}")
            }
        }
    }

    fun fetchUser() {
        editProfile = false
        coroutineScope.launch {
            checkToken(context)
            val id = fetchUserId(context)
            soundCapsuleViewModel.fetchSongs(id)
            Log.d("DEBUG_PROFILE", "Fetched user ID: $id")

            val bearerToken = "Bearer ${getToken(context)}"
            Log.d("DEBUG_PROFILE", "Bearer Token: $bearerToken")
            val user = RetrofitClient.api.getProfile(bearerToken)

            Log.d("DEBUG_PROFILE", "Fetched user: $user")
            username = user.username
            country = getCountryNameFromCode(user.location)
            profileURL = "http://34.101.226.132:3000/uploads/profile-picture/${user.profilePhoto}"

            Log.d("DEBUG_PROFILE", "profile url: $profileURL")

            val songs = songRepository.getSongsByUploader(id)
            songCountStats = songs.size
            likeCount = songRepository.getLikedSongsByUploader(id).size
            listenedCount = 0

            Log.d("DEBUG_PROFILE", "song count: $songCountStats")
            Log.d("DEBUG_PROFILE", "like count: $likeCount")
            Log.d("DEBUG_PROFILE", "listened count: $listenedCount")

            for (song in songs){
                if (song.lastPlayedDate != "") {
                    listenedCount++
                } 
            }
        }
    }

    fun logout(){
        coroutineScope.launch {
            TokenManager.clearToken(context)
            Log.d("DEBUG_PROFILE", "Logged out")
            mediaPlayerViewModel.clearCurrent()
            navController.navigate("login")
        }
    }

    LaunchedEffect(Unit) {
        fetchUser()
    }
    
    LaunchedEffect(profileURL) {
        if (profileURL.isNotBlank()) {
            val bitmap = loadBitmapFromUrl(context, profileURL)
            bitmap?.let {
                dominantColor = extractDominantColor(it)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            dominantColor,
                            Color(0xFF121212),
                            Color(0xFF121212)
                        )
                    )
                )
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))
    
            // Profile picture
            ProfilePicture(
                profileURL = profileURL,
                editProfile = editProfile,
                onEditClick = { uploadPhoto = true }
            )
    
            Spacer(modifier = Modifier.height(12.dp))
            
            // User info
            Text(username, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            Text(country, color = Color.LightGray, fontSize = 14.sp)
    
            // Edit location button
            if(editProfile) {
                IconButton(
                    onClick = { editLocation = true },
                    modifier = Modifier
                        .offset(x = 50.dp, y = (-15).dp)
                        .size(15.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_edit),
                        contentDescription = "Edit Location",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
    
            // Profile action buttons
            Spacer(modifier = Modifier.height(16.dp))
            ProfileActionButtons(
                editProfile = editProfile,
                onEditClick = { editProfile = true },
                onSaveClick = {
                    editProfile(context, locationCode.ifEmpty { null }, photoProfileFile)
                    editProfile = false
                    fetchUser()
                    photoProfileFile = null
                    locationCode = ""
                },
                onCancelClick = {
                    editProfile = false
                    fetchUser()
                },
                onLogoutClick = { logout() }
            )
    
            Spacer(modifier = Modifier.height(32.dp))
    
            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(number = songCountStats.toString(), label = "Songs")
                StatItem(number = likeCount.toString(), label = "Liked")
                StatItem(number = listenedCount.toString(), label = "Listened")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sound Capsule Section
            Text(
                text = "Your Sound Capsule",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp).align(Alignment.Start)
            )
    
            // Time listened card
            TimeListenedCard(
                timeListened = soundCapsuleViewModel.timeListened,
                onClick = { showTimeListenedModal = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Top Artist and Top Song Row
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                TopArtistCard(
                    title = soundCapsuleViewModel.topArtistName,
                    imageUri = soundCapsuleViewModel.topArtistImageUri,
                    onClick = { showTopArtistModal = true }
                )
                TopSongCard(
                    title = soundCapsuleViewModel.topSongTitle,
                    imageUri = soundCapsuleViewModel.topSongImageUri,
                    onClick = { showTopSongModal = true }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            MaxStreakCard(
                songTitle = soundCapsuleViewModel.maxStreakSongTitle,
                artistName = soundCapsuleViewModel.maxStreakArtistName,
                streakCount = soundCapsuleViewModel.maxStreakCount,
                imageUri = soundCapsuleViewModel.maxStreakImageUri,
                dateRange = soundCapsuleViewModel.maxStreakDateRange,
            )

            Spacer(modifier = Modifier.height(160.dp))
        }
        
        if (showTimeListenedModal) {
            TimeListenedDetailModal(
                onDismiss = { showTimeListenedModal = false },
                currentMonthYear = soundCapsuleViewModel.currentMonthYearForModal,
                totalMinutes = soundCapsuleViewModel.totalMinutesInMonth,
                dailyAverageMinutes = soundCapsuleViewModel.dailyAverageMinutesInMonth,
                dailyPlayback = soundCapsuleViewModel.dailyPlaybackData
            )
        }
        if (showTopArtistModal) {
            TopArtistDetailModal(
                onDismiss = { showTopArtistModal = false },
                topArtists = soundCapsuleViewModel.topArtistData,
                artistCount = soundCapsuleViewModel.artistCount
            )
        }
        if (showTopSongModal) {
            TopSongDetailModal(
                onDismiss = { showTopSongModal = false },
                topSongs = soundCapsuleViewModel.topSongData,
                songCount = soundCapsuleViewModel.songCount
            )
        }
        
        // Dialogs
        if (uploadPhoto) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    context as Activity,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    1000
                )
            }
            FileUploadDialog(
                onDismiss = { uploadPhoto = false },
                onImageSelected = { path -> profileURL = path },
                onFileSelected = { file ->
                    photoProfileFile = file
                }
            )
        }
    
        if (editLocation) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            LocationDialog(
                onDismiss = { editLocation = false },
                onUseCurrentLocation = { useCurrentLocation() },
                onPickFromMap = { 
                    val intent = Intent(context, MapPickerActivity::class.java)
                    mapLauncher.launch(intent) 
                }
            )
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun ProfilePicture(
    profileURL: String,
    editProfile: Boolean,
    onEditClick: () -> Unit
) {
    Box {
        AsyncImage(
            model = profileURL,
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        
        if (editProfile) {
            IconButton(
                onClick = onEditClick,
                modifier = Modifier
                    .defaultMinSize(minWidth = 1.dp, minHeight = 1.dp)
                    .offset(x = 50.dp, y = (-20).dp)
                    .size(15.dp)
                    .background(Color.White, CircleShape)
                    .border(1.dp, Color.LightGray, CircleShape)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_edit),
                    contentDescription = "Edit Profile Picture",
                    tint = Color.Black,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

@Composable
fun ProfileActionButtons(
    editProfile: Boolean,
    onEditClick: () -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!editProfile) {
                Button(
                    onClick = onEditClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.DarkGray,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(32.dp)
                ) {
                    Text("Edit Profile")
                }
            }
    
            if(editProfile) {
                Button(
                    onClick = onSaveClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1DB954),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(32.dp)
                ) {
                    Text("Save")
                }
                Button(
                    onClick = onCancelClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.DarkGray,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(32.dp)
                ) {
                    Text("Cancel")
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onLogoutClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.DarkGray,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(32.dp)
        ) {
            Text("Logout")
        }
    }
}

@Composable
fun StatItem(number: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = number,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label.uppercase(),
            color = Color.Gray,
            fontSize = 12.sp
        )
    }
}

@Composable
fun FileUploadDialog(
    onDismiss: () -> Unit,
    onImageSelected: (String) -> Unit,
    onFileSelected: (File?) -> Unit
) {
    val context = LocalContext.current

    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var tempFile by remember { mutableStateOf<File?>(null)}

    // Launcher: Gallery
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val path = it.toString()
            onImageSelected(path)
            val file = uriToFile(context, it)
            onFileSelected(file)
        }
        onDismiss()
    }

    // Launcher: Camera
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            val path = tempCameraUri.toString()
            onImageSelected(path)
            onFileSelected(tempFile)
        }
        onDismiss()
    }

    fun createImageFile(context: Context): Pair<File, Uri> {
        val imageFile = File.createTempFile(
            "IMG_${System.currentTimeMillis()}",
            ".jpg",
            context.cacheDir
        )
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            imageFile
        )
        return imageFile to uri
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 8.dp,
            modifier = Modifier
                .padding(16.dp)
                .border(1.dp, Color.DarkGray),
            color = Color(0xFF121212),

        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Upload Image", style = MaterialTheme.typography.titleMedium, color = Color.White)
                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = {
                    galleryLauncher.launch("image/*") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.DarkGray,
                        contentColor = Color.White
                    )) {
                    Text("Choose from Gallery")
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(onClick = {
                    val (file, uri) = createImageFile(context)
                    tempFile = file
                    tempCameraUri = uri
                    cameraLauncher.launch(uri) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.DarkGray,
                        contentColor = Color.White
                    )) {
                    Text("Take Photo")
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.DarkGray,
                        contentColor = Color.White
                    )) {
                    Text("Cancel")
                }
            }
        }
    }
}

fun uriToFile(context: Context, uri: Uri): File {
    val inputStream = context.contentResolver.openInputStream(uri)
    val file = File.createTempFile("upload", ".tmp", context.cacheDir)
    inputStream.use { input ->
        file.outputStream().use { output ->
            input?.copyTo(output)
        }
    }
    return file
}

@Composable
fun LocationDialog(
    onDismiss: () -> Unit,
    onUseCurrentLocation: () -> Unit,
    onPickFromMap: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 8.dp,
            modifier = Modifier
                .padding(16.dp)
                .border(1.dp, Color.DarkGray),
            color = Color(0xFF121212)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Pilih Lokasi", style = MaterialTheme.typography.titleMedium, color = Color.White)
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onUseCurrentLocation()
                              onDismiss()},
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.DarkGray,
                        contentColor = Color.White
                    )
                ) {
                    Text("Use Current Location")
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { onPickFromMap()
                              onDismiss()},
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.DarkGray,
                        contentColor = Color.White
                    )
                ) {
                    Text("Pick From Map")
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.DarkGray,
                        contentColor = Color.White
                    )
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}


fun getCountryCodeFromLocation(
    context: Context,
    latitude: Double,
    longitude: Double
): String? {
    return try {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)

        if (!addresses.isNullOrEmpty()) {
            addresses[0].countryCode
        } else {
            null
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
