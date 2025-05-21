package com.vlog.my.data.scripts.articles

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArticlesScriptsRepository @Inject constructor(
    private val articlesScriptsService: ArticlesScriptsService
) {

    suspend fun getItemList(url: String): Result<Any> =
        withContext(Dispatchers.IO) {
            try {
                return@withContext Result.success(articlesScriptsService.getItemListRaw(url))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

}