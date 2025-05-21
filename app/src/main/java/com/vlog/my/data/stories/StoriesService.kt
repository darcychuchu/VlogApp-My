package com.vlog.my.data.stories

import com.vlog.my.data.ApiResponse
import com.vlog.my.data.PaginatedResponse
import okhttp3.MultipartBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface StoriesService {
    // 获取全局动态和作品列表
    @GET("stories/list")
    suspend fun getStoriesList(
        @Query("typed") typed: Int = -1,
        @Query("page") page: Int = 1,
        @Query("token") token: String? = null
    ): ApiResponse<PaginatedResponse<Stories>>


    // 分享内容到动态（无需上传图片）
    @POST("{name}/stories-shared")
    suspend fun shareStories(
        @Path("name") name: String,
        @Query("token") token: String,
        @Query("title") title: String?,
        @Query("description") description: String?,
        @Query("tags") tags: String?,
        @Query("shareContent") shareContent: String,
        @Query("shareTyped") shareTyped: Int = 0
    ): ApiResponse<Any>

    // 获取用户动态列表
    @GET("{name}/stories/list")
    suspend fun getStoriesList(
        @Path("name") name: String,
        @Query("token") token: String
    ): ApiResponse<List<Stories>>

    // 获取用户动态详情
    @GET("{name}/stories/{id}")
    suspend fun getStoriesDetail(
        @Path("name") name: String,
        @Path("id") id: String,
        @Query("token") token: String?
    ): ApiResponse<Stories>

    // 发布用户动态
    @Multipart
    @POST("{name}/stories-created")
    suspend fun createStories(
        @Path("name") name: String,
        @Query("token") token: String,
        @Part photoFile: List<MultipartBody.Part>,
        @Query("title") title: String?,
        @Query("description") description: String?,
        @Query("tags") tags: String?,
        @Query("shareContent") shareContent: String?
    ): ApiResponse<Any>
}
