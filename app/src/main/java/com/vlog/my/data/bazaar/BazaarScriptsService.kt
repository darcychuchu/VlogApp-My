package com.vlog.my.data.bazaar

import com.vlog.my.data.ApiResponse
import okhttp3.MultipartBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface BazaarScriptsService {
    @GET("bazaar/scripts/list")
    suspend fun getScriptsList(
        @Query("token") token: String
    ): ApiResponse<List<BazaarScripts>>

    @Multipart
    @POST("bazaar/scripts-created")
    suspend fun createScript(
        @Query("name") name: String,
        @Query("token") token: String,
        @Part logoFile: MultipartBody.Part,
        @Query("title") title: String?,
        @Query("description") description: String?,
        @Query("tags") tags: String?,
        @Query("configs") configs: String?,
        @Query("configTyped") configTyped: Int,
        @Part databaseFile: MultipartBody.Part? // New parameter
    ): ApiResponse<Any>
}