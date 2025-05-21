package com.vlog.my.data.stories

import com.vlog.my.data.ApiResponse
import com.vlog.my.data.PaginatedResponse
import okhttp3.MultipartBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoriesRepository @Inject constructor(
    private val storiesService: StoriesService
) {
    // 获取动态列表
    suspend fun getStoriesList(
        typed: Int = -1,
        page: Int = 1,
        token: String? = null
    ): ApiResponse<PaginatedResponse<Stories>> {
        return storiesService.getStoriesList(typed, page, token)
    }

    // 分享内容到动态（无需上传图片）
    suspend fun shareStories(
        name: String,
        token: String,
        title: String?,
        description: String?,
        tags: String?,
        shareContent: String,
        shareTyped: Int = 0
    ): ApiResponse<Any> {
        return storiesService.shareStories(name, token, title, description, tags, shareContent, shareTyped)
    }


    // 获取用户动态列表
    suspend fun getStoriesList(name: String, token: String): ApiResponse<List<Stories>> {
        return storiesService.getStoriesList(name, token)
    }

    // 获取用户动态详情
    suspend fun getStoriesDetail(name: String, id: String, token: String?): ApiResponse<Stories> {
        return storiesService.getStoriesDetail(name, id, token)
    }

    // 发布用户动态
    suspend fun createStories(
        name: String,
        token: String,
        photoFiles: List<MultipartBody.Part>,
        title: String?,
        description: String?,
        tags: String?,
        shareContent: String? = null
    ): ApiResponse<Any> {
        return storiesService.createStories(name, token, photoFiles, title, description, tags, shareContent)
    }
}
