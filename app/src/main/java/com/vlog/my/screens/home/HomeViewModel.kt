package com.vlog.my.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vlog.my.data.stories.Stories
import com.vlog.my.data.stories.StoriesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val storiesRepository: StoriesRepository,
) : ViewModel() {

    // 动态列表
    private val _storiesList = MutableStateFlow<List<Stories>>(emptyList())
    val storiesList: StateFlow<List<Stories>> = _storiesList

    /**
     * 加载全局动态和作品列表
     * @param refresh 是否刷新
     */
    fun loadGlobalStoriesList(refresh: Boolean = false) {

        viewModelScope.launch {
            try {
                val response = storiesRepository.getStoriesList(
                    typed = 0
                )

                Log.d("HomeViewModel", "API响应: code=${response.code}, message=${response.message}, data=${response.data != null}")


            } catch (e: Exception) {
                Log.e("HomeViewModel", "加载全局列表失败", e)
            } finally {
            }
        }
    }

}
