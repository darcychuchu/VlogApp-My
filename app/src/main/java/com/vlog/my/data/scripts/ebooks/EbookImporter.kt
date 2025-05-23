package com.vlog.my.data.scripts.ebooks

import android.content.Context
import android.util.Log
import java.io.File
import java.io.IOException
import java.util.UUID

class EbookImporter(private val context: Context) {

    suspend fun importTxtFile(
        filePath: String,
        subScriptId: String,
        ebookTitle: String,
        author: String?,
        databaseName: String // Added databaseName parameter
    ): Ebook? { // Return type changed to Ebook?
        val file = File(filePath)
        if (!file.exists()) {
            Log.e("EbookImporter", "File not found at path: $filePath")
            throw IOException("File not found at path: $filePath")
        }

        val content = try {
            file.readText(Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e("EbookImporter", "Error reading file: $filePath", e)
            throw IOException("Error reading file: $filePath", e)
        }

        val dbHelper = EbookSqliteHelper(context, databaseName)
        val ebookId = UUID.randomUUID().toString()
        val currentTime = System.currentTimeMillis()

        val ebook = Ebook(
            id = ebookId,
            subScriptId = subScriptId,
            title = ebookTitle,
            author = author,
            filePath = filePath,
            coverImagePath = null,
            createdAt = currentTime,
            lastOpenedAt = currentTime
        )

        val chapters = mutableListOf<Chapter>()
        val lines = content.split('\n')
        var currentChapterContent = StringBuilder()
        var chapterOrder = 1
        var potentialTitle: String? = null
        var blankLineCounter = 0

        for (line in lines) {
            if (line.isBlank()) {
                blankLineCounter++
            } else {
                if (blankLineCounter >= 2 && currentChapterContent.isNotBlank()) {
                    val chapterTitle = potentialTitle?.trim() ?: "Chapter $chapterOrder"
                    chapters.add(
                        Chapter(
                            id = UUID.randomUUID().toString(),
                            ebookId = ebookId,
                            title = chapterTitle,
                            content = currentChapterContent.toString().trim(),
                            order = chapterOrder++
                        )
                    )
                    currentChapterContent.clear()
                    potentialTitle = line.trim()
                } else if (currentChapterContent.isBlank() && potentialTitle == null) {
                    potentialTitle = line.trim()
                }
                currentChapterContent.append(line).append('\n')
                blankLineCounter = 0
            }
        }

        if (currentChapterContent.isNotBlank()) {
            val chapterTitle = potentialTitle?.trim() ?: "Chapter $chapterOrder"
            chapters.add(
                Chapter(
                    id = UUID.randomUUID().toString(),
                    ebookId = ebookId,
                    title = chapterTitle,
                    content = currentChapterContent.toString().trim(),
                    order = chapterOrder++
                )
            )
        }
        
        if (chapters.isEmpty()) {
            chapters.add(
                Chapter(
                    id = UUID.randomUUID().toString(),
                    ebookId = ebookId,
                    title = "Chapter 1",
                    content = content.trim(),
                    order = 1
                )
            )
        }

        // Save to database using EbookSqliteHelper
        val ebookRowId = dbHelper.addEbook(ebook)
        if (ebookRowId == -1L) {
            Log.e("EbookImporter", "Failed to save ebook metadata for title: ${ebook.title}")
            // Consider closing dbHelper if it were Closeable, though SQLiteOpenHelper usually isn't per operation.
            return null // Indicate failure
        }
        Log.d("EbookImporter", "Successfully inserted ebook with ID: ${ebook.id}, rowId: $ebookRowId")


        dbHelper.addChapters(chapters) // Assuming this logs errors internally or throws on failure
        Log.d("EbookImporter", "Attempted to insert ${chapters.size} chapters for ebook ID: ${ebook.id}")


        val firstChapterId = chapters.firstOrNull()?.id
        val bookmark = if (firstChapterId != null) {
            Bookmark(
                id = UUID.randomUUID().toString(),
                ebookId = ebookId,
                chapterId = firstChapterId,
                pageNumber = 1,
                progressPercentage = 0.0f,
                lastReadAt = currentTime
            )
        } else {
            Bookmark(
                id = UUID.randomUUID().toString(),
                ebookId = ebookId,
                chapterId = null,
                pageNumber = null,
                progressPercentage = 0.0f,
                lastReadAt = currentTime
            )
        }
        val bookmarkRowId = dbHelper.addBookmark(bookmark)
        if (bookmarkRowId == -1L) {
            Log.w("EbookImporter", "Failed to save initial bookmark for ebook ID: ${ebook.id}")
            // Not returning null here as ebook and chapters are saved, but logging a warning.
        } else {
            Log.d("EbookImporter", "Successfully inserted bookmark with ID: ${bookmark.id}, rowId: $bookmarkRowId for ebook ID: ${ebook.id}")
        }

        // dbHelper.close() // Not standard practice to close SQLiteOpenHelper after each set of operations.
                         // It's managed by Android and designed to be longer-lived.

        return ebook
    }

    suspend fun importPdfFile(
        filePath: String,
        subScriptId: String,
        ebookTitle: String,
        author: String?,
        databaseName: String
    ): Ebook? {
        val file = File(filePath)
        if (!file.exists()) {
            Log.e("EbookImporter", "PDF File not found at path: $filePath")
            throw IOException("PDF File not found at path: $filePath")
        }

        val fullPdfText: String = try {
            withContext(Dispatchers.IO) { // Ensure PDFBox operations are off the main thread
                val document = com.tom_roush.pdfbox.pdmodel.PDDocument.load(file)
                val pdfStripper = com.tom_roush.pdfbox.text.PDFTextStripper()
                val text = pdfStripper.getText(document)
                document.close()
                text
            }
        } catch (e: Exception) {
            Log.e("EbookImporter", "Error parsing PDF '$filePath': ${e.message}", e)
            return null // Return null on PDF parsing error
        }

        if (fullPdfText.isBlank()) {
            Log.w("EbookImporter", "Extracted text from PDF '$filePath' is blank.")
            // Optionally, still create an ebook entry with an empty chapter or return null
            // For now, let's proceed to create an ebook with an empty chapter
        }
        
        val dbHelper = EbookSqliteHelper(context, databaseName)
        val ebookId = UUID.randomUUID().toString()
        val currentTime = System.currentTimeMillis()

        val ebook = Ebook(
            id = ebookId,
            subScriptId = subScriptId,
            title = ebookTitle,
            author = author,
            filePath = filePath, // Store original PDF file path
            coverImagePath = null, // Cover handling is separate
            createdAt = currentTime,
            lastOpenedAt = currentTime
        )

        // Create a single chapter for the entire PDF content
        val chapter = Chapter(
            id = UUID.randomUUID().toString(),
            ebookId = ebookId,
            title = ebookTitle, // Or "Full Document" or similar
            content = fullPdfText,
            order = 1
        )

        // Save Ebook metadata
        val ebookRowId = dbHelper.addEbook(ebook)
        if (ebookRowId == -1L) {
            Log.e("EbookImporter", "Failed to save PDF-based ebook metadata for title: ${ebook.title}")
            return null
        }
        Log.d("EbookImporter", "Successfully inserted PDF-based ebook with ID: ${ebook.id}, rowId: $ebookRowId")

        // Save the single chapter
        dbHelper.addChapters(listOf(chapter)) // Use addChapters with a list containing one chapter
        Log.d("EbookImporter", "Inserted single chapter for PDF-based ebook ID: ${ebook.id}")

        // Create and save initial bookmark
        val bookmark = Bookmark(
            id = UUID.randomUUID().toString(),
            ebookId = ebookId,
            chapterId = chapter.id, // Point to the single chapter
            pageNumber = 1, // Default to page 1
            progressPercentage = 0.0f,
            lastReadAt = currentTime
        )
        val bookmarkRowId = dbHelper.addBookmark(bookmark)
        if (bookmarkRowId == -1L) {
            Log.w("EbookImporter", "Failed to save initial bookmark for PDF-based ebook ID: ${ebook.id}")
        } else {
            Log.d("EbookImporter", "Successfully inserted bookmark for PDF-based ebook ID: ${ebook.id}")
        }

        return ebook
    }
}
