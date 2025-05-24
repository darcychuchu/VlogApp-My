package com.vlog.my.screens.subscripts.articles

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

// Basic data class for an article (can be expanded)
data class ArticleData(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val author: String? = null,
    val databaseName: String? = null // If articles are stored per-script
)

class AddArticleViewModel : ViewModel() {
    var articleTitle by mutableStateOf("")
    var articleContent by mutableStateOf("")
    var articleAuthor by mutableStateOf("") // Optional
    var articleDbName by mutableStateOf("") // Optional, for consistency

    // Placeholder for saving logic
    fun saveArticle(onSuccess: () -> Unit) {
        viewModelScope.launch {
            // TODO: Implement actual saving logic:
            // 1. Get current user if needed for 'createdBy'
            // 2. Use a DataHelper (e.g., SubScriptsDataHelper or a new ArticlesDataHelper)
            //    to store the article data.
            // 3. The ArticleData class might need to align with SubScripts or a new table structure.
            // For now, just print or log.
            println("Saving Article: Title='${articleTitle}', Content='${articleContent}', Author='${articleAuthor}', DB='${articleDbName}'")
            onSuccess()
        }
    }
}
