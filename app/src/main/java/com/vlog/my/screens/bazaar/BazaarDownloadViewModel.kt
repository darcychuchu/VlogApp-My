package com.vlog.my.screens.bazaar

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vlog.my.data.bazaar.BazaarScripts
import com.vlog.my.data.scripts.ContentType
import com.vlog.my.data.scripts.SubScripts
import com.vlog.my.data.scripts.SubScriptsDataHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

class BazaarDownloadViewModel(
    application: Application,
    private val subScriptsDataHelper: SubScriptsDataHelper
) : AndroidViewModel(application) {

    private val httpClient = OkHttpClient()
    private val EBOOK_TYPE_ID = ContentType.EBOOK.typeId // Assuming ContentType.EBOOK.typeId is 4

    suspend fun downloadAndIntegrateEbook(bazaarScript: BazaarScripts): Boolean {
        return withContext(Dispatchers.IO) {
            if (bazaarScript.databaseUrl.isNullOrBlank()) {
                Log.e("BazaarDownloadVM", "Database URL is null or blank for BazaarScript ID: ${bazaarScript.id}")
                return@withContext false
            }
            if (bazaarScript.id.isNullOrBlank()) {
                Log.e("BazaarDownloadVM", "BazaarScript ID is null or blank. Cannot proceed.")
                return@withContext false
            }

            val localDbName = "bazaar_ebook_${bazaarScript.id}_${UUID.randomUUID().toString().substring(0, 8)}.db"
            val context = getApplication<Application>().applicationContext
            val localDbFile = context.getDatabasePath(localDbName)

            // Ensure parent directory exists
            if (!localDbFile.parentFile.exists()) {
                if (!localDbFile.parentFile.mkdirs()) {
                    Log.e("BazaarDownloadVM", "Failed to create database directory: ${localDbFile.parentFile}")
                    return@withContext false
                }
            }

            // Download the file
            try {
                val request = Request.Builder().url(bazaarScript.databaseUrl!!).build()
                httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        Log.e("BazaarDownloadVM", "Failed to download database file from ${bazaarScript.databaseUrl}. Code: ${response.code}")
                        throw IOException("Failed to download file: ${response.code} ${response.message}")
                    }
                    response.body?.byteStream()?.use { inputStream ->
                        FileOutputStream(localDbFile).use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    } ?: throw IOException("Response body is null")
                }
                Log.d("BazaarDownloadVM", "Successfully downloaded database to: ${localDbFile.absolutePath}")
            } catch (e: Exception) {
                Log.e("BazaarDownloadVM", "Error downloading database from ${bazaarScript.databaseUrl}: ${e.message}", e)
                localDbFile.delete() // Clean up partial download
                return@withContext false
            }

            // Create local SubScript entry
            val newSubScript = SubScripts(
                id = "bazaar_${bazaarScript.id}", // Prefix to denote origin and use original ID
                name = bazaarScript.title ?: "Downloaded Ebook",
                isTyped = EBOOK_TYPE_ID,
                databaseName = localDbName.removeSuffix(".db"), // Store without .db extension
                mappingConfig = "{}", // Ebooks have empty mapping config
                createdBy = bazaarScript.createdBy ?: "Bazaar",
                logoUrl = bazaarScript.attachmentId, // Assuming attachmentId can be used as logoUrl or similar
                isEnabled = 1, // Enable by default
                isLocked = bazaarScript.isLocked ?: 0,
                isValued = bazaarScript.isValued ?: 0,
                version = bazaarScript.version
            )

            try {
                // Check if a script with this ID already exists, if so, maybe update or warn user
                val existing = subScriptsDataHelper.getUserScriptsById(newSubScript.id!!)
                if (existing != null) {
                    // Potentially delete old DB file if replacing
                    val oldDbFile = context.getDatabasePath("${existing.databaseName}.db")
                    if (oldDbFile.exists()) {
                        oldDbFile.delete()
                        Log.d("BazaarDownloadVM", "Deleted old database file: ${oldDbFile.absolutePath}")
                    }
                    subScriptsDataHelper.updateUserScripts(newSubScript) // Update existing
                    Log.d("BazaarDownloadVM", "Updated existing SubScript entry: ${newSubScript.id}")
                } else {
                    subScriptsDataHelper.insertUserScripts(newSubScript)
                    Log.d("BazaarDownloadVM", "Inserted new SubScript entry: ${newSubScript.id}")
                }
                return@withContext true
            } catch (e: Exception) {
                Log.e("BazaarDownloadVM", "Error saving new SubScript entry for ${newSubScript.id}: ${e.message}", e)
                localDbFile.delete() // Clean up downloaded DB file if SubScript creation fails
                return@withContext false
            }
        }
    }
}
