package com.vlog.my.screens.videos

import android.content.Context
import android.net.Uri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import java.io.File

class VideoCompressionWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val KEY_INPUT_URI = "KEY_INPUT_URI"
        const val KEY_OUTPUT_PATH = "KEY_OUTPUT_PATH"
        const val KEY_COMPRESSION_OPTION = "KEY_COMPRESSION_OPTION"
        const val KEY_OUTPUT_VIDEO_PATH = "KEY_OUTPUT_VIDEO_PATH" // For result
        const val KEY_ERROR_MESSAGE = "KEY_ERROR_MESSAGE" // For result in case of failure
    }

    override suspend fun doWork(): Result {
        val inputUriString = inputData.getString(KEY_INPUT_URI) ?: return Result.failure(
            workDataOf(KEY_ERROR_MESSAGE to "Input URI missing")
        )
        val outputPath = inputData.getString(KEY_OUTPUT_PATH) ?: return Result.failure(
            workDataOf(KEY_ERROR_MESSAGE to "Output path missing")
        )
        val compressionOption = inputData.getString(KEY_COMPRESSION_OPTION) ?: return Result.failure(
            workDataOf(KEY_ERROR_MESSAGE to "Compression option missing")
        )
//
//        // Ensure the output directory exists
//        val outputFile = File(outputPath)
//        outputFile.parentFile?.mkdirs()
//
//        val command = when (compressionOption) {
//            "640p" -> "-i \"${Uri.parse(inputUriString)}\" -vf \"scale=640:-2\" -c:v libx264 -preset medium -crf 23 -c:a aac -strict experimental \"$outputPath\""
//            "720p" -> "-i \"${Uri.parse(inputUriString)}\" -vf \"scale=-2:720\" -c:v libx264 -preset medium -crf 23 -c:a aac -strict experimental \"$outputPath\""
//            "1080p" -> "-i \"${Uri.parse(inputUriString)}\" -vf \"scale=-2:1080\" -c:v libx264 -preset medium -crf 23 -c:a aac -strict experimental \"$outputPath\""
//            else -> return Result.failure(workDataOf(KEY_ERROR_MESSAGE to "Invalid compression option"))
//        }
//
//        // FFmpegKit.execute() can be blocking, but CoroutineWorker handles this on a background thread.
//        // For long operations, consider using FFmpegKit.executeAsync() and managing callbacks,
//        // or ensure setProgressAsync is called if granular progress is needed from within FFmpegKit.
//        // However, for this worker, we'll use the synchronous execute and rely on WorkManager's progress reporting if needed from the ViewModel.
//
//        val session = FFmpegKit.execute(command)
//
//        return if (ReturnCode.isSuccess(session.returnCode)) {
//            Result.success(workDataOf(KEY_OUTPUT_VIDEO_PATH to outputPath))
//        } else {
//            val errorMessage = "FFmpeg execution failed with return code ${session.returnCode}. Logs: ${session.allLogsAsString}"
//            // It's good practice to delete the potentially incomplete/corrupt output file on failure
//            if (outputFile.exists()) {
//                outputFile.delete()
//            }
//            Result.failure(workDataOf(KEY_ERROR_MESSAGE to errorMessage))
//        }

        return Result.failure(workDataOf(KEY_ERROR_MESSAGE to "Invalid compression option"))
    }
}
