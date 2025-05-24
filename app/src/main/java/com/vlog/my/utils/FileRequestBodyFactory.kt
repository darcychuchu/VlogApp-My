package com.vlog.my.utils

import android.content.Context
import android.util.Log
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileNotFoundException

object FileRequestBodyFactory {
    private const val TAG = "FileRequestBodyFactory"

    fun createFilePart(context: Context, filePath: String?, partName: String): MultipartBody.Part? {
        if (filePath.isNullOrBlank()) {
            Log.w(TAG, "File path is null or blank for part: $partName")
            return null
        }
        return try {
            var file = File(filePath)
            if (!file.exists() || !file.isFile) {
                // If filePath is not absolute or doesn't exist, try treating it as a database name.
                // This is common for database files stored in the app's private database directory.
                Log.d(TAG, "File not found at direct path: ${file.absolutePath}. Attempting to find as database.")
                val dbFile = context.getDatabasePath(filePath) // filePath here is treated as db name
                if (dbFile != null && dbFile.exists() && dbFile.isFile) {
                    Log.d(TAG, "Found file as database: ${dbFile.absolutePath}")
                    file = dbFile
                } else {
                    val nonExistenceReason = if (dbFile == null) "dbFile object is null" else if (!dbFile.exists()) "dbFile does not exist" else "dbFile is not a file"
                    Log.e(TAG, "File not found at path: ${file.absolutePath} or as database (tried ${dbFile?.absolutePath ?: filePath}). Reason: $nonExistenceReason")
                    throw FileNotFoundException("File not found at path: ${file.absolutePath} or as database (tried ${dbFile?.absolutePath ?: filePath})")
                }
            } else {
                 Log.d(TAG, "Found file at direct path: ${file.absolutePath}")
            }

            val requestBody = file.asRequestBody("application/octet-stream".toMediaTypeOrNull())
            MultipartBody.Part.createFormData(partName, file.name, requestBody)

        } catch (e: FileNotFoundException) {
            Log.e(TAG, "FileNotFoundException for part '$partName' with path '$filePath': ${e.message}", e)
            // Optionally log error or handle it more gracefully
            null
        } catch (e: Exception) {
            Log.e(TAG, "Exception for part '$partName' with path '$filePath': ${e.message}", e)
            null
        }
    }
}
