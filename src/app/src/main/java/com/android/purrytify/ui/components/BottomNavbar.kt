package com.android.purrytify.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.android.purrytify.R

data class BottomNavItem(
    val route: String,
    val label: String,
    val activeIcon: Int,
    val inactiveIcon: Int,
    val activeColor: Color,
    val inactiveColor: Color
)

@Composable
fun BottomNavbar(navController: NavController, modifier: Modifier) {
    val items = listOf(
        BottomNavItem("home", "Home", R.drawable.ic_home_active, R.drawable.ic_home_inactive, Color.White, Color.Gray),
        BottomNavItem("library", "Your Library", R.drawable.ic_library_active, R.drawable.ic_library_inactive, Color.White, Color.Gray),
        BottomNavItem("profile", "Profile", R.drawable.ic_profile_active, R.drawable.ic_profile_inactive, Color.White, Color.Gray),
    )

    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route
    val navbarBg = if (currentRoute == "nowPlaying") Color.Transparent else  Color(0xFF121212)


    NavigationBar(
        modifier = modifier,
        containerColor = navbarBg
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route || (currentRoute == "chart" && item.route == "home")
            val textColor = if (isSelected) item.activeColor else item.inactiveColor

            NavigationBarItem(
                selected = false,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo("home") { inclusive = false }
                        }
                    }
                },
                icon = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(if (isSelected) item.activeIcon else item.inactiveIcon),
                            contentDescription = item.label,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.label,
                            color = textColor,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 14.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}
