package com.vlog.my.data.scripts.articles

import retrofit2.http.GET
import retrofit2.http.Url

/**
 * 小程序JSON Articles服务接口
 */
interface ArticlesScriptsService {
    @GET
    suspend fun getItemListRaw(@Url url: String): String
}