package com.vlog.my.data.bazaar

import com.vlog.my.data.ApiResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BazaarScriptsRepository @Inject constructor(
    private val bazaarScriptsDao: BazaarScriptsDao,
    private val bazaarScriptsService: BazaarScriptsService
) {

    suspend fun getScriptsList(token: String): ApiResponse<List<BazaarScripts>> {
        return bazaarScriptsService.getScriptsList(token)
    }

    suspend fun createScript(
        name: String,
        token: String,
        logoFile: MultipartBody.Part,
        title: String?,
        description: String,
        tags: String?,
        mappingConfig: String,
        configTyped: Int = 0,
        databaseFile: MultipartBody.Part? // New parameter
    ): ApiResponse<Any> {
        return bazaarScriptsService.createScript(
            name = name,
            token = token,
            logoFile = logoFile,
            title = title,
            description = description,
            tags = tags,
            configs = mappingConfig,
            configTyped = configTyped,
            databaseFile = databaseFile // Pass new parameter
        )
    }

    // Local operations
    fun findAllBazaarScripts(): Flow<List<BazaarScripts>> {
        return bazaarScriptsDao.findAllBazaarScripts().map { entities ->
            entities.map { it.toBazaarScripts() }
        }
    }

    suspend fun getBazaarScriptsById(id: String): BazaarScripts? {
        return bazaarScriptsDao.getBazaarScriptsById(id)?.toBazaarScripts()
    }

    suspend fun insertBazaarScripts(bazaarScript: BazaarScripts) {
        bazaarScriptsDao.insertBazaarScripts(BazaarScriptsEntity.fromBazaarScripts(bazaarScript))
    }

    suspend fun updateBazaarScripts(bazaarScript: BazaarScripts) {
        bazaarScriptsDao.updateBazaarScripts(BazaarScriptsEntity.fromBazaarScripts(bazaarScript))
    }

    suspend fun deleteBazaarScripts(bazaarScript: BazaarScripts) {
        bazaarScriptsDao.deleteBazaarScripts(BazaarScriptsEntity.fromBazaarScripts(bazaarScript))
    }

}
