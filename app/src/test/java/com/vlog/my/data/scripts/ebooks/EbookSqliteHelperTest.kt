package com.vlog.my.data.scripts.ebooks

import android.os.Build
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P]) // Configure Robolectric for a specific SDK level
class EbookSqliteHelperTest {

    private lateinit var dbHelper: EbookSqliteHelper
    private val testSubScriptId = "test_sub_script_1"
    private val testDbName = "test_ebook_db.db" // Robolectric uses in-memory if name is null, but specific name is fine

    @Before
    fun setUp() {
        // Using ApplicationProvider.getApplicationContext() for context
        // Robolectric handles in-memory database setup when a name is provided.
        // Alternatively, for a purely in-memory DB without file backing, pass null as dbName.
        // dbHelper = EbookSqliteHelper(ApplicationProvider.getApplicationContext(), null)
        dbHelper = EbookSqliteHelper(ApplicationProvider.getApplicationContext(), testDbName)
        // Manually trigger onCreate if not using a named db that Robolectric auto-creates
        // val db = dbHelper.writableDatabase // This will trigger onCreate if db doesn't exist
        // dbHelper.onCreate(db) // Not needed if writableDatabase call above triggers it
    }

    @After
    @Throws(IOException::class)
    fun tearDown() {
        dbHelper.close()
        // Robolectric should clean up the in-memory database, but explicit deletion can be done if needed for named dbs
        ApplicationProvider.getApplicationContext<android.content.Context>().deleteDatabase(testDbName)
    }

    // --- Ebook Tests ---
    @Test
    fun testAddAndGetEbook() {
        val ebook = Ebook("ebook1", testSubScriptId, "Test Ebook 1", "Author 1", "/path/to/ebook1", null, System.currentTimeMillis(), System.currentTimeMillis())
        dbHelper.addEbook(ebook)

        val retrievedEbook = dbHelper.getEbook("ebook1")
        assertNotNull(retrievedEbook)
        assertEquals("Test Ebook 1", retrievedEbook.title)
        assertEquals("Author 1", retrievedEbook.author)
        assertEquals(testSubScriptId, retrievedEbook.subScriptId)
    }

    @Test
    fun testGetEbooksForSubScript() {
        val ebook1 = Ebook("ebook1", testSubScriptId, "Ebook 1 S1", "A1", "/p1", null, System.currentTimeMillis(), System.currentTimeMillis())
        val ebook2 = Ebook("ebook2", "other_sub_id", "Ebook 2 OS1", "A2", "/p2", null, System.currentTimeMillis(), System.currentTimeMillis() + 1000)
        val ebook3 = Ebook("ebook3", testSubScriptId, "Ebook 3 S1", "A3", "/p3", null, System.currentTimeMillis(), System.currentTimeMillis() + 2000)

        dbHelper.addEbook(ebook1)
        dbHelper.addEbook(ebook2)
        dbHelper.addEbook(ebook3)

        val s1Ebooks = dbHelper.getEbooksForSubScript(testSubScriptId)
        assertEquals(2, s1Ebooks.size)
        assertTrue(s1Ebooks.any { it.id == "ebook1" })
        assertTrue(s1Ebooks.any { it.id == "ebook3" })
        // Test ordering (most recent lastOpenedAt first)
        assertEquals("ebook3", s1Ebooks[0].id)
        assertEquals("ebook1", s1Ebooks[1].id)
    }

    @Test
    fun testUpdateEbook() {
        val ebook = Ebook("ebook1", testSubScriptId, "Original Title", "Original Author", "/path", null, System.currentTimeMillis(), System.currentTimeMillis())
        dbHelper.addEbook(ebook)

        val updatedEbook = ebook.copy(title = "Updated Title", lastOpenedAt = System.currentTimeMillis() + 1000)
        val rowsAffected = dbHelper.updateEbook(updatedEbook)
        assertEquals(1, rowsAffected)

        val retrievedEbook = dbHelper.getEbook("ebook1")
        assertNotNull(retrievedEbook)
        assertEquals("Updated Title", retrievedEbook.title)
        assertEquals(updatedEbook.lastOpenedAt, retrievedEbook.lastOpenedAt)
    }

    @Test
    fun testDeleteEbook() {
        val ebook = Ebook("ebook1", testSubScriptId, "Title", "Author", "/path", null, System.currentTimeMillis(), System.currentTimeMillis())
        dbHelper.addEbook(ebook)
        // Add a chapter to test cascade delete
        val chapter = Chapter("chap1", "ebook1", "Chapter 1", "Content", 1)
        dbHelper.addChapter(chapter)


        assertNotNull(dbHelper.getEbook("ebook1"))
        assertNotNull(dbHelper.getChapter("chap1"))


        val rowsAffected = dbHelper.deleteEbook("ebook1")
        assertEquals(1, rowsAffected)
        assertNull(dbHelper.getEbook("ebook1"))
        // Verify cascade delete for chapters
        assertTrue(dbHelper.getChaptersForEbook("ebook1").isEmpty(), "Chapters should be deleted by cascade")
    }


    // --- Chapter Tests ---
    @Test
    fun testAddAndGetChaptersForEbook() {
        val ebook = Ebook("ebook_c1", testSubScriptId, "Ebook For Chapters", "Author", "/path", null, System.currentTimeMillis(), System.currentTimeMillis())
        dbHelper.addEbook(ebook)

        val chapter1 = Chapter("chap1_c1", "ebook_c1", "Chapter 1", "Content 1", 1)
        val chapter2 = Chapter("chap2_c1", "ebook_c1", "Chapter 2", "Content 2", 2)
        dbHelper.addChapters(listOf(chapter1, chapter2))

        val chapters = dbHelper.getChaptersForEbook("ebook_c1")
        assertEquals(2, chapters.size)
        assertEquals("Chapter 1", chapters[0].title)
        assertEquals(1, chapters[0].order)
        assertEquals("Chapter 2", chapters[1].title)
        assertEquals(2, chapters[1].order)
    }

    @Test
    fun testDeleteChaptersForEbook() {
        val ebook = Ebook("ebook_c2", testSubScriptId, "Ebook For Chapters Delete", "Author", "/path", null, System.currentTimeMillis(), System.currentTimeMillis())
        dbHelper.addEbook(ebook)
        val chapter1 = Chapter("chap1_c2", "ebook_c2", "C1", "C1", 1)
        dbHelper.addChapter(chapter1)

        assertEquals(1, dbHelper.getChaptersForEbook("ebook_c2").size)
        val rowsAffected = dbHelper.deleteChaptersForEbook("ebook_c2")
        assertEquals(1, rowsAffected)
        assertTrue(dbHelper.getChaptersForEbook("ebook_c2").isEmpty())
    }

    // --- Bookmark Tests ---
    @Test
    fun testAddAndGetBookmarkForEbook() {
        val ebook = Ebook("ebook_b1", testSubScriptId, "Ebook For Bookmark", "Author", "/path", null, System.currentTimeMillis(), System.currentTimeMillis())
        val chapter = Chapter("chap1_b1", "ebook_b1", "C1", "Content", 1)
        dbHelper.addEbook(ebook)
        dbHelper.addChapter(chapter)

        val bookmark = Bookmark("bm1", "ebook_b1", "chap1_b1", 10, 0.5f, System.currentTimeMillis())
        dbHelper.addBookmark(bookmark)

        val retrievedBookmark = dbHelper.getBookmarkForEbook("ebook_b1")
        assertNotNull(retrievedBookmark)
        assertEquals("bm1", retrievedBookmark.id)
        assertEquals("chap1_b1", retrievedBookmark.chapterId)
        assertEquals(10, retrievedBookmark.pageNumber)
    }

    @Test
    fun testAddAndUpdateBookmark() { // Testing add then update
        val ebook = Ebook("ebook_b2", testSubScriptId, "Ebook B2", "A", "/p", null, System.currentTimeMillis(), System.currentTimeMillis())
        val chapter1 = Chapter("chap1_b2", "ebook_b2", "C1", "Content", 1)
        val chapter2 = Chapter("chap2_b2", "ebook_b2", "C2", "Content", 2)
        dbHelper.addEbook(ebook)
        dbHelper.addChapter(chapter1)
        dbHelper.addChapter(chapter2)

        val initialBookmark = Bookmark("bm_initial", "ebook_b2", "chap1_b2", 5, 0.2f, System.currentTimeMillis())
        dbHelper.addBookmark(initialBookmark)

        var retrieved = dbHelper.getBookmark("bm_initial")
        assertNotNull(retrieved)
        assertEquals(5, retrieved.pageNumber)

        val updatedBookmark = initialBookmark.copy(chapterId = "chap2_b2", pageNumber = 1, progressPercentage = 0.01f, lastReadAt = System.currentTimeMillis() + 100)
        dbHelper.updateBookmark(updatedBookmark) // updateBookmark works on bookmark.id

        retrieved = dbHelper.getBookmark("bm_initial") // Get by same ID
        assertNotNull(retrieved)
        assertEquals("chap2_b2", retrieved.chapterId)
        assertEquals(1, retrieved.pageNumber)
        assertEquals(0.01f, retrieved.progressPercentage)

        // Test getBookmarkForEbook still gets this one (as it's the only one)
        val currentEbookBookmark = dbHelper.getBookmarkForEbook("ebook_b2")
        assertNotNull(currentEbookBookmark)
        assertEquals("bm_initial", currentEbookBookmark.id)
    }
    
    @Test
    fun testGetBookmarkForEbook_ReturnsMostRecent() {
        val ebook = Ebook("ebook_b3", testSubScriptId, "Ebook B3", "A", "/p", null, System.currentTimeMillis(), System.currentTimeMillis())
        dbHelper.addEbook(ebook)

        val oldBookmark = Bookmark("bm_old", "ebook_b3", null, 1, 0.1f, System.currentTimeMillis() - 10000)
        val newBookmark = Bookmark("bm_new", "ebook_b3", null, 2, 0.2f, System.currentTimeMillis())
        dbHelper.addBookmark(oldBookmark)
        dbHelper.addBookmark(newBookmark) // Add a newer one

        val retrieved = dbHelper.getBookmarkForEbook("ebook_b3")
        assertNotNull(retrieved)
        assertEquals("bm_new", retrieved.id) // Should retrieve the one with the latest lastReadAt
    }


    // --- FontSetting Tests ---
    @Test
    fun testAddOrUpdateAndGetFontSetting() {
        val setting1 = FontSetting(testSubScriptId, 18)
        dbHelper.addOrUpdateFontSetting(setting1)

        var retrievedSetting = dbHelper.getFontSetting(testSubScriptId)
        assertNotNull(retrievedSetting)
        assertEquals(18, retrievedSetting.fontSize)

        val setting2 = FontSetting(testSubScriptId, 22) // Same subScriptId, different size
        dbHelper.addOrUpdateFontSetting(setting2)

        retrievedSetting = dbHelper.getFontSetting(testSubScriptId)
        assertNotNull(retrievedSetting)
        assertEquals(22, retrievedSetting.fontSize) // Should be updated
    }
}
