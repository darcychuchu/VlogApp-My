package com.vlog.my.screens.scripts.ebooks

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vlog.my.data.scripts.ebooks.Bookmark
import com.vlog.my.data.scripts.ebooks.Chapter
import com.vlog.my.data.scripts.ebooks.Ebook
import com.vlog.my.data.scripts.ebooks.EbookSqliteHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class EbookReaderViewModel(
    application: Application,
    private val subScriptId: String, // Needed for font settings
    private val ebookId: String,
    private val databaseName: String
) : AndroidViewModel(application) {

    private val dbHelper = EbookSqliteHelper(application, databaseName)

    private val _ebookDetails = MutableStateFlow<Ebook?>(null)
    val ebookDetails: StateFlow<Ebook?> = _ebookDetails.asStateFlow()

    private val _chapters = MutableStateFlow<List<Chapter>>(emptyList())
    val chapters: StateFlow<List<Chapter>> = _chapters.asStateFlow()

    private val _currentChapter = MutableStateFlow<Chapter?>(null)
    val currentChapter: StateFlow<Chapter?> = _currentChapter.asStateFlow()

    private val _currentBookmark = MutableStateFlow<Bookmark?>(null)
    val currentBookmark: StateFlow<Bookmark?> = _currentBookmark.asStateFlow()

    private val _fontSize = MutableStateFlow(16) // Default font size
    val fontSize: StateFlow<Int> = _fontSize.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val currentChapterContent: StateFlow<String> = combine(currentChapter) { chapter ->
        chapter?.firstOrNull()?.content ?: ""
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.Lazily, "")


    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                withContext(Dispatchers.IO) {
                    _ebookDetails.value = dbHelper.getEbook(ebookId)
                    _chapters.value = dbHelper.getChaptersForEbook(ebookId)
                    val fontSetting = dbHelper.getFontSetting(subScriptId) // subScriptId for font settings
                    _fontSize.value = fontSetting?.fontSize ?: 16
                    _currentBookmark.value = dbHelper.getBookmarkForEbook(ebookId)

                    // Determine initial chapter based on bookmark or first chapter
                    val initialChapterId = _currentBookmark.value?.chapterId ?: _chapters.value.firstOrNull()?.id
                    if (initialChapterId != null) {
                        _currentChapter.value = _chapters.value.find { it.id == initialChapterId }
                    } else if (_chapters.value.isNotEmpty()) {
                        _currentChapter.value = _chapters.value.first()
                    }
                    Log.d("EbookReaderVM", "Initial load: Ebook: ${_ebookDetails.value?.title}, Chapters: ${_chapters.value.size}, Bookmark: ${_currentBookmark.value != null}, Chapter: ${_currentChapter.value?.title}")
                }
            } catch (e: Exception) {
                Log.e("EbookReaderVM", "Error loading initial data for ebook $ebookId in db $databaseName: ${e.message}", e)
                // Handle error states, perhaps by emitting error flows or values
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectChapter(chapterId: String) {
        viewModelScope.launch {
            val selectedChapter = _chapters.value.find { it.id == chapterId }
            if (selectedChapter != null) {
                _currentChapter.value = selectedChapter
                // Save bookmark for this newly selected chapter
                saveBookmarkProgress(chapterId, pageNumber = 1, progressPercentage = 0.0f) // Reset progress for new chapter
                Log.d("EbookReaderVM", "Selected chapter: ${selectedChapter.title}")

                 // Update lastOpenedAt for the ebook
                _ebookDetails.value?.let { ebook ->
                    val updatedEbook = ebook.copy(lastOpenedAt = System.currentTimeMillis())
                     withContext(Dispatchers.IO) {
                        dbHelper.updateEbook(updatedEbook)
                    }
                    _ebookDetails.value = updatedEbook
                }

            } else {
                Log.w("EbookReaderVM", "Chapter ID $chapterId not found in loaded chapters.")
            }
        }
    }

    // Renamed to avoid confusion with the currentBookmark StateFlow
    fun saveBookmarkProgress(chapterId: String, pageNumber: Int?, progressPercentage: Float?) {
         viewModelScope.launch(Dispatchers.IO) {
            val existingBookmark = currentBookmark.value // Use the one from StateFlow
            val bookmarkToSave: Bookmark

            if (existingBookmark != null && existingBookmark.ebookId == ebookId) {
                // Update existing bookmark for this ebook
                bookmarkToSave = existingBookmark.copy(
                    chapterId = chapterId,
                    pageNumber = pageNumber,
                    progressPercentage = progressPercentage,
                    lastReadAt = System.currentTimeMillis()
                )
                dbHelper.updateBookmark(bookmarkToSave)
                Log.d("EbookReaderVM", "Updated bookmark for ebook $ebookId, chapter $chapterId")
            } else {
                // Create new bookmark for this ebook
                bookmarkToSave = Bookmark(
                    id = UUID.randomUUID().toString(), // Generate new ID for new bookmark
                    ebookId = ebookId,
                    chapterId = chapterId,
                    pageNumber = pageNumber,
                    progressPercentage = progressPercentage,
                    lastReadAt = System.currentTimeMillis()
                )
                dbHelper.addBookmark(bookmarkToSave) // This should be an upsert or careful add
                Log.d("EbookReaderVM", "Added new bookmark for ebook $ebookId, chapter $chapterId")
            }
             _currentBookmark.value = bookmarkToSave // Update the StateFlow
        }
    }
}
