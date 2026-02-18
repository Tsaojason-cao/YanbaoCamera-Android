package com.yanbao.camera.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.yanbao.camera.R
import com.yanbao.camera.ui.components.DesignSpec
import com.yanbao.camera.ui.components.GlassCard
import com.yanbao.camera.ui.components.KuromiCorners
import com.yanbao.camera.ui.components.SearchBar

/**
 * 首页 - 推荐卡片瀑布流
 * 
 * 设计规范：
 * - 背景：蓝紫渐变
 * - 顶部：搜索栏（毛玻璃，56dp高，28dp圆角）
 * - 内容：推荐卡片瀑布流（LazyColumn）
 *   - 卡片圆角16dp，白色毛玻璃，内边距16dp，间距12dp
 *   - 卡片包含：用户头像（48dp）、用户名、图片（16:9）、点赞/评论/分享
 * - 底部：5按钮导航栏（相机按钮特殊处理）
 * - 四角库洛米装饰
 */

// Mock数据
data class RecommendItem(
    val id: Int,
    val userName: String,
    val imageUrl: String,
    val description: String,
    val likes: Int = 1200,
    val comments: Int = 345,
    val avatarUrl: String = ""
)

val mockRecommendList = listOf(
    RecommendItem(
        1,
        "Golden Hour in Kyoto",
        "https://images.unsplash.com/photo-1493246507139-91e8fad9978e?w=400&h=300&fit=crop",
        "Beautiful sunset at Kyoto temple"
    ),
    RecommendItem(
        2,
        "Urban Street Art",
        "https://images.unsplash.com/photo-1460661419201-fd4cecdf8a8b?w=400&h=300&fit=crop",
        "Colorful street art in the city"
    ),
    RecommendItem(
        3,
        "Cozy Home Vibe",
        "https://images.unsplash.com/photo-1495521821757-a1efb6729352?w=400&h=300&fit=crop",
        "Breakfast and coffee at home"
    ),
    RecommendItem(
        4,
        "Nature Vibes",
        "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=400&h=300&fit=crop",
        "Mountain landscape photography"
    ),
    RecommendItem(
        5,
        "City Lights",
        "https://images.unsplash.com/photo-1449824913935-59a10b8d2000?w=400&h=300&fit=crop",
        "Night city skyline view"
    )
)

@Composable
fun HomeScreen(
    currentRoute: String = "home",
    onNavigate: (String) -> Unit = {},
    onNavigateToDetail: (Int) -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF9FB1D6), // 蓝紫
                        Color(0xFFD8A5D6), // 紫粉
                        Color(0xFFF5D0D6)  // 浅粉
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 顶部标题
            Text(
                text = "Yanbao Camera",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.CenterHorizontally)
            )
            
            // 搜索栏
            SearchBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                placeholder = "Search photos, users, or tags...",
                onSearch = {}
            )
            
            // 推荐卡片列表
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(mockRecommendList) { item ->
                    RecommendCardItem(
                        item = item,
                        onClick = { onNavigateToDetail(item.id) }
                    )
                }
            }
        }
        
        // 库洛米四角装饰
        KuromiCorners()
    }
}

@Composable
fun RecommendCardItem(
    item: RecommendItem,
    onClick: () -> Unit = {}
) {
    val isLiked = remember { mutableStateOf(false) }
    
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        cornerRadius = 16.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 用户信息行
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 用户头像
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            color = Color(0xFFEC4899).copy(alpha = 0.3f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("👤", fontSize = 24.sp)
                }
                
                // 用户名
                Text(
                    text = item.userName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }
            
            // 图片
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Gray.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.description,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            
            // 互动按钮行
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 点赞
                Row(
                    modifier = Modifier.clickable {
                        isLiked.value = !isLiked.value
                    },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isLiked.value) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isLiked.value) Color(0xFFEC4899) else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "${item.likes}",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                
                // 评论
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_comment),
                        contentDescription = "Comment",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "${item.comments}",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                
                // 分享
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_share),
                        contentDescription = "Share",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Share",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
    }
}
