package com.vlog.my.screens.subscripts.articles

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

// Assuming ArticleData data class is already defined (e.g., in AddArticleViewModel.kt or a shared location)
// If not, define it here:
// data class ArticleData(val id: String = "", val title: String = "", val content: String = "", val author: String? = null, val databaseName: String? = null)

class EditArticleViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val articleId: String = savedStateHandle.get<String>("articleId") ?: ""

    var articleTitle by mutableStateOf("")
    var articleContent by mutableStateOf("")
    var articleAuthor by mutableStateOf("")
    // var articleDbName by mutableStateOf("") // Usually not editable

    init {
        if (articleId.isNotEmpty()) {
            loadArticleDetails(articleId)
        }
    }

    private fun loadArticleDetails(id: String) {
        viewModelScope.launch {
            // TODO: Implement actual data loading logic from a repository/dataHelper
            // For now, using placeholder data:
            println("Loading article with ID: $id")
            val placeholderData = ArticleData(
                id = id, 
                title = "Sample Article Title for ID $id", 
                content = "This is the sample content for the article.", 
                author = "Sample Author"
            )
            articleTitle = placeholderData.title
            articleContent = placeholderData.content
            articleAuthor = placeholderData.author ?: ""
            // articleDbName = placeholderData.databaseName ?: ""
        }
    }

    fun updateArticle(onSuccess: () -> Unit) {
        viewModelScope.launch {
            // TODO: Implement actual update logic:
            // 1. Use a DataHelper to update the article data for articleId.
            println("Updating Article: ID='${articleId}', Title='${articleTitle}', Content='${articleContent}', Author='${articleAuthor}'")
            onSuccess()
        }
    }
}
