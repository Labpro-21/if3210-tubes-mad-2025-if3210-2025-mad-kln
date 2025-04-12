package com.android.purrytify.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.android.purrytify.data.local.entities.Song
import kotlinx.coroutines.launch
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.purrytify.data.local.RepositoryProvider
import com.android.purrytify.ui.adapter.SongAdapter
import com.android.purrytify.ui.components.SongUploadButton
import com.android.purrytify.view_model.LibraryViewModel
import com.android.purrytify.view_model.PlayerViewModel
import com.android.purrytify.view_model.getLibraryViewModel
import fetchUserId
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce

@Composable
fun LibraryScreen(
    mediaPlayerViewModel: PlayerViewModel,
    libraryViewModel: LibraryViewModel = getLibraryViewModel()
) {
    val recyclerViewRef = remember { mutableStateOf<RecyclerView?>(null) }
    val songAdapterRef = remember { mutableStateOf<SongAdapter?>(null) }

    val tab by remember { libraryViewModel::tab }
    val activeSongs = libraryViewModel.activeSongs

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val userId = remember { mutableStateOf(0) }
    val searchQuery = remember { mutableStateOf("") }

    fun fetchUser() {
        coroutineScope.launch {
            Log.d("LibraryScreen", "Fetching user ID")
            userId.value = fetchUserId(context)
            Log.d("LibraryScreen", "Fetched user ID: ${userId.value}")
            libraryViewModel.fetchLibrary(userId.value, searchQuery.value)
        }
    }

    LaunchedEffect(Unit) {
        fetchUser()
    }

    LaunchedEffect(activeSongs) {
        mediaPlayerViewModel.setSongs(activeSongs)
        songAdapterRef.value?.updateSongs(activeSongs)
    }

    Scaffold(
        containerColor = Color(0xFF121212),
        contentWindowInsets = WindowInsets(0.dp),
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            PageHeader({ libraryViewModel.fetchAllSongs(userId.value) })
            SearchBox(
                onChange = { query ->
                    Log.d("SearchLogger", "User typed: $query")
                    libraryViewModel.fetchLibrary(userId.value, query)
                }
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StyledButton(
                    text = "All",
                    isSelected = tab == "all",
                    onClick = {
                        libraryViewModel.switchTab("all")
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                StyledButton(
                    text = "Liked",
                    isSelected = tab == "liked",
                    onClick = {
                        libraryViewModel.switchTab("liked")
                    }
                )
            }

            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                thickness = 1.dp,
                color = Color.Gray
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(490.dp)
                    .clipToBounds()
            ) {
                key(activeSongs) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { context ->
                            RecyclerView(context).apply {
                                layoutManager = LinearLayoutManager(context)
                                adapter = SongAdapter(activeSongs) { song ->
                                    val index = activeSongs.indexOf(song)
                                    mediaPlayerViewModel.playSong(context, index)
                                    Log.d("LibraryScreen", "Playing Song: ${song.title} - ${song.artist}")
                                }.also { songAdapterRef.value = it }
                                recyclerViewRef.value = this
                            }
                        }
                    )
                }
            }

        }
    }
}

@Composable
fun PageHeader(func: () -> Unit) {
    Row (modifier = Modifier
        .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom) {
        Text(
            "Your Library",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(start = 16.dp, top = 40.dp, bottom = 8.dp)
        )
        SongUploadButton(func)
    }
}

@Composable
fun StyledButton(
    text: String,
    isSelected: Boolean,
    onClick: (() -> Unit)? = null
) {
    val backgroundColor = if (isSelected) Color(0xFF1DB954) else Color(0xFF333333)
    val textColor = if (isSelected) Color.Black else Color.White

    Button(
        onClick = { onClick?.invoke() },
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(50.dp),
        modifier = Modifier
            .height(35.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            modifier =  Modifier.padding(horizontal = 24.dp),
        )
    }
}

@OptIn(FlowPreview::class)
@Composable
fun SearchBox(
    onChange: (String) -> Unit
) {
    var searchText by remember { mutableStateOf("") }
    val debounceDelay = 500L

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 0.dp)) {
        TextField(
            value = searchText,
            onValueChange = { searchText = it },
            placeholder = { Text("Search") },
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color(0xFF212121),
                unfocusedContainerColor = Color(0xFF212121),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Color.White
            ),
            shape = RoundedCornerShape(32.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(0.dp, Color.DarkGray, RoundedCornerShape(32.dp)),
            textStyle = TextStyle(fontSize = 14.sp),
        )
    }


    LaunchedEffect(Unit) {
        snapshotFlow { searchText }
            .debounce(debounceDelay)
            .collect { query ->
                onChange(query)
            }
    }
}
