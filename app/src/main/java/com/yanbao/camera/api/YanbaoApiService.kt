package com.yanbao.camera.api

import com.yanbao.camera.model.LocationCard
import com.yanbao.camera.model.Post
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

/**
 * 雁宝API服务接口
 */
interface YanbaoApiService {
    
    /**
     * 获取推荐流
     */
    @GET("api/posts/recommended")
    suspend fun getRecommendedPosts(
        @Query("page") page: Int,
        @Query("size") size: Int = 10
    ): ApiResponse<PagedResponse<Post>>
    
    /**
     * 获取推荐位置
     */
    @GET("api/locations/recommended")
    suspend fun getRecommendedLocations(
        @Query("category") category: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): ApiResponse<PagedResponse<LocationCard>>
    
    /**
     * 上传作品
     */
    @Multipart
    @POST("api/posts")
    suspend fun uploadPost(
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody,
        @Part("locationId") locationId: RequestBody? = null
    ): ApiResponse<Post>
}
