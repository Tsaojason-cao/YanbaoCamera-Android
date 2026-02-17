package com.yanbao.camera.repository

import com.yanbao.camera.model.LocationCard
import com.yanbao.camera.model.Post
import com.yanbao.camera.model.User
import com.yanbao.camera.model.UserStats

/**
 * Mock数据仓库 - 用于开发和测试
 */
object MockDataRepository {

    /**
     * 获取当前用户
     */
    fun getCurrentUser(): User {
        return User(
            id = "user_001",
            name = "摄影师小王",
            avatar = "https://via.placeholder.com/150",
            bio = "热爱摄影，记录生活的美好瞬间 📸",
            followers = 1250,
            following = 380,
            postCount = 156,
            likeCount = 8920
        )
    }

    /**
     * 获取用户统计信息
     */
    fun getUserStats(): UserStats {
        return UserStats(
            totalPhotos = 156,
            totalLikes = 8920,
            totalViews = 45300,
            totalComments = 1230,
            favoriteCount = 320
        )
    }

    /**
     * 获取推荐流Post列表
     */
    fun getRecommendedPosts(page: Int = 0, pageSize: Int = 10): List<Post> {
        val posts = mutableListOf<Post>()
        val baseIndex = page * pageSize

        repeat(pageSize) { index ->
            val id = baseIndex + index
            posts.add(
                Post(
                    id = "post_$id",
                    userId = "user_${id % 5}",
                    userName = listOf("小李", "小张", "小刘", "小王", "小陈")[id % 5],
                    userAvatar = "https://via.placeholder.com/100",
                    imageUrl = "https://via.placeholder.com/400x500",
                    title = "美景分享 #${id}",
                    description = "这是一张美丽的风景照片，拍摄于${listOf("北京", "上海", "杭州", "南京", "苏州")[id % 5]}。",
                    likes = (Math.random() * 5000).toInt(),
                    comments = (Math.random() * 500).toInt(),
                    shares = (Math.random() * 200).toInt(),
                    timestamp = System.currentTimeMillis() - (id * 3600000),
                    location = listOf("北京", "上海", "杭州", "南京", "苏州")[id % 5],
                    tags = listOf("风景", "摄影", "旅游", "美景").shuffled().take(2)
                )
            )
        }

        return posts
    }

    /**
     * 获取推荐位置列表
     */
    fun getRecommendedLocations(): List<LocationCard> {
        return listOf(
            LocationCard(
                id = "loc_001",
                name = "故宫",
                description = "北京的标志性建筑，拥有丰富的历史文化",
                imageUrl = "https://via.placeholder.com/300x200",
                latitude = 39.9163,
                longitude = 116.3972,
                rating = 4.8f,
                postCount = 12500
            ),
            LocationCard(
                id = "loc_002",
                name = "西湖",
                description = "杭州最美的景点，四季风景各不相同",
                imageUrl = "https://via.placeholder.com/300x200",
                latitude = 30.2741,
                longitude = 120.1551,
                rating = 4.7f,
                postCount = 8900
            ),
            LocationCard(
                id = "loc_003",
                name = "外滩",
                description = "上海的经典景观，夜景美不胜收",
                imageUrl = "https://via.placeholder.com/300x200",
                latitude = 31.2304,
                longitude = 121.4737,
                rating = 4.6f,
                postCount = 10200
            ),
            LocationCard(
                id = "loc_004",
                name = "夫子庙",
                description = "南京的文化中心，古色古香",
                imageUrl = "https://via.placeholder.com/300x200",
                latitude = 32.0603,
                longitude = 118.7969,
                rating = 4.5f,
                postCount = 6800
            ),
            LocationCard(
                id = "loc_005",
                name = "苏州园林",
                description = "世界文化遗产，精致的古典园林",
                imageUrl = "https://via.placeholder.com/300x200",
                latitude = 31.2989,
                longitude = 120.5954,
                rating = 4.7f,
                postCount = 7500
            )
        )
    }

    /**
     * 获取推荐用户列表
     */
    fun getRecommendedUsers(): List<User> {
        return listOf(
            User(
                id = "user_001",
                name = "风景摄影师",
                avatar = "https://via.placeholder.com/100",
                bio = "专注风景摄影",
                followers = 5200,
                postCount = 450
            ),
            User(
                id = "user_002",
                name = "人像摄影",
                avatar = "https://via.placeholder.com/100",
                bio = "人像摄影爱好者",
                followers = 3800,
                postCount = 320
            ),
            User(
                id = "user_003",
                name = "美食摄影",
                avatar = "https://via.placeholder.com/100",
                bio = "记录美食的美妙",
                followers = 2900,
                postCount = 280
            ),
            User(
                id = "user_004",
                name = "夜景摄影",
                avatar = "https://via.placeholder.com/100",
                bio = "夜景摄影专家",
                followers = 4100,
                postCount = 380
            ),
            User(
                id = "user_005",
                name = "微距摄影",
                avatar = "https://via.placeholder.com/100",
                bio = "探索微观世界",
                followers = 2100,
                postCount = 220
            )
        )
    }
}
