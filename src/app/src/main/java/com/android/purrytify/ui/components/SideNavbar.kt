package com.android.purrytify.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.android.purrytify.R
import com.android.purrytify.data.local.entities.Song
import com.android.purrytify.ui.modal.MiniPlayer
import com.android.purrytify.view_model.PlayerViewModel

@Composable
fun SideNavbar(
    navController: NavController,
    mediaPlayerViewModel: PlayerViewModel,
    currentSong: Song?,
    onOpenFullPlayer: () -> Unit,
    currentRoute: String?,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomNavItem("home", "Home", R.drawable.ic_home_active, R.drawable.ic_home_inactive, Color.White, Color.Gray),
        BottomNavItem("library", "Your Library", R.drawable.ic_library_active, R.drawable.ic_library_inactive, Color.White, Color.Gray),
        BottomNavItem("qr_scanner", "Scan QR", R.drawable.ic_qr_scanner, R.drawable.ic_qr_scanner_inactive, Color.White, Color.Gray),
        BottomNavItem("profile", "Profile", R.drawable.ic_profile_active, R.drawable.ic_profile_inactive, Color.White, Color.Gray),
    )

//    val navBackStackEntry by navController.currentBackStackEntryAsState()

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(240.dp)
            .background(Color(0xFF121212))
            .padding(vertical = 16.dp)
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route || (currentRoute == "chart" && item.route == "home")
            val contentColor = if (isSelected) item.activeColor else item.inactiveColor

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .clickable {
                        if (currentRoute != item.route) {
                            navController.navigate(item.route) {
                                popUpTo("home") { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    }
            ) {
                Image(
                    painter = painterResource(if (isSelected) item.activeIcon else item.inactiveIcon),
                    contentDescription = item.label,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = item.label,
                    color = contentColor,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Start,
                    lineHeight = 18.sp,
                )
            }
        }

        Spacer(Modifier.weight(1f))

        if (currentSong != null && currentRoute != "nowPlaying" && currentRoute != "login" && currentRoute != "blank") {
            MiniPlayer(
                viewModel = mediaPlayerViewModel,
                onOpenFullPlayer = onOpenFullPlayer,
            )
        }
    }
}