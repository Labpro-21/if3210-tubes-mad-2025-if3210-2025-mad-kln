package com.android.purrytify.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
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
fun BottomNavbar(navController: NavController) {
    val items = listOf(
        BottomNavItem("login", "Login", R.drawable.ic_home_active, R.drawable.ic_home_inactive,  Color.White, Color.Gray),
        BottomNavItem("home", "Home", R.drawable.ic_home_active, R.drawable.ic_home_inactive, Color.White, Color.Gray),
        BottomNavItem("library", "Your Library", R.drawable.ic_library_active, R.drawable.ic_library_inactive, Color.White, Color.Gray),
        BottomNavItem("profile", "Profile", R.drawable.ic_profile_active, R.drawable.ic_profile_inactive, Color.White, Color.Gray)
    )

    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    NavigationBar(
        modifier = Modifier
            .fillMaxWidth(),
        containerColor = Color(0xFF121212)
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route
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
                    Image(
                        painter = painterResource(if (isSelected) item.activeIcon else item.inactiveIcon),
                        contentDescription = item.label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = { Text(item.label, color = textColor) },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}
