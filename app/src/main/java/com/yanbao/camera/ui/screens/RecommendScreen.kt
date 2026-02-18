package com.yanbao.camera.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.yanbao.camera.ui.components.GlassCard
import com.yanbao.camera.ui.components.KuromiCorners

data class RecommendPlace(
    val id: Int,
    val title: String,
    val category: String,
    val description: String,
    val imageUrl: String,
    val rating: Float
)

val recommendPlaces = listOf(
    RecommendPlace(1, "喵星人咖啡馆", "宠物互动", "治愈系猫咪陪伴，咖啡香醇，快来撸猫！", "https://images.unsplash.com/photo-1519052537078-e6302a4968d4?w=200&h=200&fit=crop", 4.8f),
    RecommendPlace(2, "星光夜市", "地道美食", "汇聚各地特色小吃，热闹非凡，吃货必打卡！", "https://images.unsplash.com/photo-1504674900967-77b2e0a9b4d4?w=200&h=200&fit=crop", 4.8f),
    RecommendPlace(3, "樱花谷公园", "自然风光", "春日赏樱胜地，如梦如幻，拍照打卡佳地！", "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=200&h=200&fit=crop", 4.8f),
    RecommendPlace(4, "未来艺术空间", "艺术展览", "沉浸式数字艺术体验，科技与艺术的完美融合！", "https://images.unsplash.com/photo-1493246507139-91e8fad9978e?w=200&h=200&fit=crop", 4.8f),
    RecommendPlace(5, "海滨度假村", "度假休闲", "碧海蓝天，沙滩美景，度假首选！", "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=200&h=200&fit=crop", 4.7f),
    RecommendPlace(6, "古镇文化街", "文化体验", "传统文化与现代艺术碰撞，感受历史魅力！", "https://images.unsplash.com/photo-1495521821757-a1efb6729352?w=200&h=200&fit=crop", 4.6f)
)

val categories = listOf("附近", "最新", "热门", "评分")

@Composable
fun RecommendScreen(
    currentRoute: String = "recommend",
    onNavigate: (String) -> Unit = {}
) {
    val selectedCategory = remember { mutableStateOf(0) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFA78BFA),
                        Color(0xFFEC4899),
                        Color(0xFFF9A8D4)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp)
        ) {
            // 顶部标题 + 分类栏
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "AI推荐",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    cornerRadius = 24.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .horizontalScroll(rememberScrollState())
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        categories.forEachIndexed { index, category ->
                            Box(
                                modifier = Modifier
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(
                                        if (selectedCategory.value == index)
                                            Color(0xFFEC4899)
                                        else
                                            Color.White.copy(alpha = 0.2f)
                                    )
                                    .clickable { selectedCategory.value = index }
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = category,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
            
            // 推荐卡片列表（2列网格）
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(recommendPlaces.size) { index ->
                    val place = recommendPlaces[index]
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.95f))
                            .clickable { }
                            .padding(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Gray.copy(alpha = 0.3f))
                        ) {
                            AsyncImage(
                                model = place.imageUrl,
                                contentDescription = place.title,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                            
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFDB022))
                                    .padding(4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = "⭐",
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = place.rating.toString(),
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        
                        Text(
                            text = place.title,
                            color = Color.Black,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        
                        Text(
                            text = place.category,
                            color = Color(0xFFEC4899),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        
                        Text(
                            text = place.description,
                            color = Color.Gray,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp),
                            maxLines = 2
                        )
                    }
                }
            }
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .align(Alignment.BottomCenter)
                .background(
                    color = Color.White.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                )
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onNavigate("home") }
            ) {
                Icon(
                    imageVector = Icons.Filled.Home,
                    contentDescription = "首页",
                    tint = Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
                Text("首页", color = Color.Gray, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onNavigate("camera") }
            ) {
                Icon(
                    imageVector = Icons.Filled.PhotoCamera,
                    contentDescription = "相机",
                    tint = Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
                Text("相机", color = Color.Gray, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = "推荐",
                    tint = Color(0xFFEC4899),
                    modifier = Modifier.size(24.dp)
                )
                Text("推荐", color = Color(0xFFEC4899), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onNavigate("gallery") }
            ) {
                Icon(
                    imageVector = Icons.Filled.Image,
                    contentDescription = "图库",
                    tint = Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
                Text("图库", color = Color.Gray, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onNavigate("profile") }
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "我的",
                    tint = Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
                Text("我的", color = Color.Gray, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
            }
        }
        
        KuromiCorners()
    }
}
