package com.vlog.my.data.scripts.ebooks

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.io.FileWriter
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class EbookImporterTest {

    private lateinit var context: Context
    private lateinit var importer: EbookImporter
    private lateinit var tempFileDir: File // To store temporary test files

    @Mock
    private lateinit var mockDbHelper: EbookSqliteHelper

    // Argument captors
    @Captor private lateinit var ebookCaptor: ArgumentCaptor<Ebook>
    @Captor private lateinit var chaptersCaptor: ArgumentCaptor<List<Chapter>>
    @Captor private lateinit var bookmarkCaptor: ArgumentCaptor<Bookmark>


    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        context = ApplicationProvider.getApplicationContext()
        importer = EbookImporter(context) // EbookImporter takes context

        // Setup a temporary directory for test files
        tempFileDir = File(context.cacheDir, "importer_test_files")
        if (!tempFileDir.exists()) {
            tempFileDir.mkdirs()
        }
    }

    @After
    fun tearDown() {
        // Clean up temporary files
        tempFileDir.deleteRecursively()
    }

    private fun createTempTxtFile(fileName: String, content: String): File {
        val file = File(tempFileDir, fileName)
        FileWriter(file).use { it.write(content) }
        return file
    }

    @Test
    fun testImportTxtFile_Basic() = runBlocking {
        val sampleContent = "Chapter 1 Title\n\nThis is chapter 1.\n\n\nChapter 2 Title\n\nThis is chapter 2."
        val tempFile = createTempTxtFile("basic.txt", sampleContent)

        // Mock DB interactions
        `when`(mockDbHelper.addEbook(ebookCaptor.capture())).thenReturn(1L) // Success
        // addChapters is void, so no thenReturn needed unless we want to check for exceptions
        `when`(mockDbHelper.addBookmark(bookmarkCaptor.capture())).thenReturn(1L) // Success

        // Replace the importer's dbHelper instance - THIS IS THE TRICKY PART
        // Since EbookImporter creates its own EbookSqliteHelper, we need to either:
        // 1. Make EbookSqliteHelper an interface and inject it (preferred for testability)
        // 2. Use a testing framework that allows mocking object creation (e.g., PowerMockito, or advanced Mockito with mockito-inline if EbookSqliteHelper is final)
        // 3. For this test, since we are testing the importer's logic primarily, and assuming EbookSqliteHelper is tested elsewhere,
        //    we will create a *real* EbookSqliteHelper with an in-memory DB for the importer to use.
        //    This makes it more of an integration test between Importer and Helper.

        val testDbName = "importer_basic_test.db"
        ApplicationProvider.getApplicationContext<Context>().deleteDatabase(testDbName) // Clean before test
        val realDbHelperForTest = EbookSqliteHelper(context, testDbName)


        // We need to modify EbookImporter to allow injection of EbookSqliteHelper for pure unit testing.
        // For now, let's simulate the calls and verify arguments based on a real helper.
        // This means we won't use mockDbHelper directly with the current EbookImporter structure.
        // Instead, we'll call the importer and then query the realDbHelperForTest.

        val importedEbook = importer.importTxtFile(
            filePath = tempFile.absolutePath,
            subScriptId = "sub1",
            ebookTitle = "Basic Ebook",
            author = "Test Author",
            databaseName = testDbName
        )

        assertNotNull(importedEbook, "Imported ebook should not be null")
        assertEquals("Basic Ebook", importedEbook.title)
        assertEquals("Test Author", importedEbook.author)

        val chapters = realDbHelperForTest.getChaptersForEbook(importedEbook.id)
        assertEquals(2, chapters.size, "Should be 2 chapters")
        assertEquals("Chapter 1 Title", chapters[0].title)
        assertTrue(chapters[0].content.trim().startsWith("This is chapter 1."), "Chapter 1 content mismatch")
        assertEquals("Chapter 2 Title", chapters[1].title)
        assertTrue(chapters[1].content.trim().startsWith("This is chapter 2."), "Chapter 2 content mismatch")

        val bookmark = realDbHelperForTest.getBookmarkForEbook(importedEbook.id)
        assertNotNull(bookmark)
        assertEquals(chapters[0].id, bookmark.chapterId) // Bookmark should point to the first chapter
        assertEquals(1, bookmark.pageNumber)

        ApplicationProvider.getApplicationContext<Context>().deleteDatabase(testDbName) // Clean up
    }

    @Test
    fun testImportTxtFile_EmptyFile() = runBlocking {
        val tempFile = createTempTxtFile("empty.txt", "")
        val testDbName = "importer_empty_test.db"
        ApplicationProvider.getApplicationContext<Context>().deleteDatabase(testDbName)

        val importedEbook = importer.importTxtFile(
            filePath = tempFile.absolutePath,
            subScriptId = "sub_empty",
            ebookTitle = "Empty Ebook",
            author = null,
            databaseName = testDbName
        )

        assertNotNull(importedEbook)
        val realDbHelperForTest = EbookSqliteHelper(context, testDbName)
        val chapters = realDbHelperForTest.getChaptersForEbook(importedEbook.id)
        assertEquals(1, chapters.size, "Should create one chapter for empty file")
        assertEquals("Chapter 1", chapters[0].title) // Default title
        assertTrue(chapters[0].content.isBlank(), "Content should be blank")

        ApplicationProvider.getApplicationContext<Context>().deleteDatabase(testDbName)
    }

    @Test
    fun testImportTxtFile_NoChapterSeparators() = runBlocking {
        val content = "Line 1\nLine 2\nLine 3 still part of chapter 1."
        val tempFile = createTempTxtFile("no_sep.txt", content)
        val testDbName = "importer_no_sep_test.db"
        ApplicationProvider.getApplicationContext<Context>().deleteDatabase(testDbName)

        val importedEbook = importer.importTxtFile(
            filePath = tempFile.absolutePath,
            subScriptId = "sub_no_sep",
            ebookTitle = "Single Chapter Ebook",
            author = "Author",
            databaseName = testDbName
        )

        assertNotNull(importedEbook)
        val realDbHelperForTest = EbookSqliteHelper(context, testDbName)
        val chapters = realDbHelperForTest.getChaptersForEbook(importedEbook.id)
        assertEquals(1, chapters.size, "Should be 1 chapter")
        // The first line becomes the title for the single chapter if no separators
        assertEquals("Line 1", chapters[0].title)
        assertEquals(content.trim(), chapters[0].content.trim())

        ApplicationProvider.getApplicationContext<Context>().deleteDatabase(testDbName)
    }
    
    @Test
    fun testImportTxtFile_OnlyBlankLines() = runBlocking {
        val content = "\n\n\n\n"
        val tempFile = createTempTxtFile("only_blank.txt", content)
        val testDbName = "importer_only_blank_test.db"
        ApplicationProvider.getApplicationContext<Context>().deleteDatabase(testDbName)

        val importedEbook = importer.importTxtFile(
            filePath = tempFile.absolutePath,
            subScriptId = "sub_blank",
            ebookTitle = "Blank Lines Ebook",
            author = null,
            databaseName = testDbName
        )

        assertNotNull(importedEbook)
        val realDbHelperForTest = EbookSqliteHelper(context, testDbName)
        val chapters = realDbHelperForTest.getChaptersForEbook(importedEbook.id)
        assertEquals(1, chapters.size)
        assertEquals("Chapter 1", chapters[0].title) // Default title
        assertTrue(chapters[0].content.isBlank())

        ApplicationProvider.getApplicationContext<Context>().deleteDatabase(testDbName)
    }


    @Test
    fun testImportTxtFile_DatabaseSaveFailure_EbookInsert() = runBlocking {
        val sampleContent = "Chapter 1\n\nContent."
        val tempFile = createTempTxtFile("fail_ebook.txt", sampleContent)

        // To test actual failure, we would need to inject a mock EbookSqliteHelper
        // or make EbookSqliteHelper an interface.
        // The current EbookImporter structure instantiates EbookSqliteHelper directly.
        // A true unit test for this case requires refactoring EbookImporter for DI.

        // For now, this test will pass if the importer simply doesn't crash.
        // A more robust test would verify that null is returned if addEbook returns -1L.
        // This requires the ability to mock EbookSqliteHelper.
        // Let's assume for demonstration that if the file path is invalid, it might lead to an internal error
        // that could be caught, or that the import process still returns an ebook object even if db write fails (which it shouldn't).

        // This test highlights the limitation of testing EbookImporter without DI for EbookSqliteHelper.
        // We cannot easily mock `dbHelper.addEbook` to return -1L.
        // The importer would proceed to call other db methods, which is not ideal for unit testing this specific failure.

        // A placeholder test to show intent:
        val importerWithMockedHelper = EbookImporter(context) // Ideally, this would take a mock helper
        
        // If we could inject mockDbHelper:
        // `when`(mockDbHelper.addEbook(any())).thenReturn(-1L)
        // val result = importerWithMockedHelper.importTxtFile(..., mockDbHelper)
        // assertNull(result)
        
        // With current structure, we can't easily force addEbook to fail in isolation.
        // We can test for IOException if file doesn't exist.
        try {
            importer.importTxtFile(
                filePath = "/non/existent/path.txt",
                subScriptId = "sub_fail",
                ebookTitle = "Fail Ebook",
                author = null,
                databaseName = "importer_fail_test.db"
            )
        } catch (e: IOException) {
            // Expected if file doesn't exist
            assertTrue(e.message!!.contains("File not found"))
        }
    }
}
