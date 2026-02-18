package com.yanbao.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.yanbao.camera.ui.navigation.NavRoutes
import com.yanbao.camera.ui.navigation.NavGraph

@Composable
fun YanbaoAppContent() {
    val navController = rememberNavController()
    var currentRoute by remember { mutableStateOf(NavRoutes.SPLASH) }

    Box(modifier = Modifier.fillMaxSize()) {
        // 导航图
        NavGraph(
            navController = navController,
            startDestination = NavRoutes.SPLASH,
            onRouteChanged = { currentRoute = it }
        )

        // 底部导航栏（仅在非启动屏显示）
        if (currentRoute != NavRoutes.SPLASH) {
            BottomNavigationBar(
                modifier = Modifier.align(Alignment.BottomCenter),
                currentRoute = currentRoute,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(NavRoutes.HOME) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Composable
fun BottomNavigationBar(
    modifier: Modifier = Modifier,
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(
                color = Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            )
            .blur(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 首页
            NavBarItem(
                icon = Icons.Filled.Home,
                label = "首页",
                isSelected = currentRoute == NavRoutes.HOME,
                onClick = { onNavigate(NavRoutes.HOME) }
            )

            // 相机
            NavBarItem(
                icon = Icons.Filled.PhotoCamera,
                label = "相机",
                isSelected = currentRoute == NavRoutes.CAMERA,
                onClick = { onNavigate(NavRoutes.CAMERA) }
            )

            // 推荐
            NavBarItem(
                icon = Icons.Filled.Favorite,
                label = "推荐",
                isSelected = currentRoute == NavRoutes.RECOMMEND,
                onClick = { onNavigate(NavRoutes.RECOMMEND) }
            )

            // 相册
            NavBarItem(
                icon = Icons.Filled.Image,
                label = "相册",
                isSelected = currentRoute == NavRoutes.GALLERY,
                onClick = { onNavigate(NavRoutes.GALLERY) }
            )

            // 我的
            NavBarItem(
                icon = Icons.Filled.Person,
                label = "我的",
                isSelected = currentRoute == NavRoutes.PROFILE,
                onClick = { onNavigate(NavRoutes.PROFILE) }
            )
        }
    }
}

@Composable
fun NavBarItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) Color(0xFFEC4899) else Color.White,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = label,
                color = if (isSelected) Color(0xFFEC4899) else Color.White,
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
