package com.yanbao.camera.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

sealed class BottomNavItem(val route: String, val label: String, val icon: ImageVector) {
    object Home : BottomNavItem("home", "首页", Icons.Filled.Home)
    object Camera : BottomNavItem("camera", "相机", Icons.Filled.PhotoCamera)
    object Gallery : BottomNavItem("gallery", "相册", Icons.Filled.Collections)
    object Recommend : BottomNavItem("recommend", "推荐", Icons.Filled.LocationOn)
    object Profile : BottomNavItem("profile", "我的", Icons.Filled.Person)
}

val bottomNavItems = listOf(
    BottomNavItem.Home,
    BottomNavItem.Camera,
    BottomNavItem.Gallery,
    BottomNavItem.Recommend,
    BottomNavItem.Profile
)

@Composable
fun YanbaoBottomNavigation(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = { onNavigate(item.route) }
            )
        }
    }
}
