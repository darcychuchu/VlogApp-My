package com.vlog.my.screens.bazaar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vlog.my.data.bazaar.BazaarScripts
import com.vlog.my.data.bazaar.BazaarScriptsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BazaarScriptsViewModel @Inject constructor(
    private val bazaarScriptsRepository: BazaarScriptsRepository
) : ViewModel() {

    // 提供对BazaarScriptsRepository的公共访问
    val repository: BazaarScriptsRepository
        get() = bazaarScriptsRepository

    // UI状态
    private val _uiState = MutableStateFlow(BazaarScriptsUiState())
    val uiState: StateFlow<BazaarScriptsUiState> = _uiState.asStateFlow()

    // 选中的标签页
    private val _selectedTabIndex = MutableStateFlow(0)
    val selectedTabIndex: StateFlow<Int> = _selectedTabIndex.asStateFlow()

    // 服务器脚本列表
    private val _serverScripts = MutableStateFlow<List<BazaarScripts>>(emptyList())
    val serverScripts: StateFlow<List<BazaarScripts>> = _serverScripts.asStateFlow()

    // 本地脚本列表
    private val _localScripts = MutableStateFlow<List<BazaarScripts>>(emptyList())
    val localScripts: StateFlow<List<BazaarScripts>> = _localScripts.asStateFlow()

    // 选中的脚本（用于预览或详情查看）
    private val _selectedScript = MutableStateFlow<BazaarScripts?>(null)
    val selectedScript: StateFlow<BazaarScripts?> = _selectedScript.asStateFlow()

    // 初始化
    init {
        loadLocalScripts()
    }

    // 切换标签页
    fun selectTab(index: Int) {
        _selectedTabIndex.value = index
        if (index == 0 && _serverScripts.value.isEmpty()) {
            loadServerScripts()
        } else if (index == 1) {
            loadLocalScripts()
        }
    }

    // 加载服务器脚本列表
    fun loadServerScripts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // 这里使用一个临时token，实际应用中应该从用户认证系统获取
                val response = bazaarScriptsRepository.getScriptsList("temp_token")
                if (response.code == 200){
                    _serverScripts.value = response.data ?: emptyList()
                    _uiState.value = _uiState.value.copy(isLoading = false)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = response.message
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "未知错误"
                )
            }
        }
    }

    // 加载本地脚本列表
    fun loadLocalScripts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                bazaarScriptsRepository.findAllBazaarScripts().collectLatest { scripts ->
                    _localScripts.value = scripts
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "未知错误"
                )
            }
        }
    }

    // 下载脚本到本地
    fun downloadScript(script: BazaarScripts) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // 检查本地是否已存在该脚本
                val localScript = bazaarScriptsRepository.getBazaarScriptsById(script.id ?: "")
                
                if (localScript == null) {
                    // 如果本地不存在，直接插入
                    bazaarScriptsRepository.insertBazaarScripts(script)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "下载成功"
                    )
                } else {
                    // 如果本地已存在，检查版本
                    if (localScript.version < script.version) {
                        // 服务器版本更新，提示用户
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            showUpdateDialog = true,
                            scriptToUpdate = script
                        )
                    } else {
                        // 版本相同或本地版本更高
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            message = "已是最新版本"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "下载失败"
                )
            }
        }
    }

    // 更新本地脚本
    fun updateScript(script: BazaarScripts) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                bazaarScriptsRepository.updateBazaarScripts(script)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "更新成功",
                    showUpdateDialog = false,
                    scriptToUpdate = null
                )
                // 刷新本地列表
                loadLocalScripts()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "更新失败"
                )
            }
        }
    }

    // 删除本地脚本
    fun deleteScript(script: BazaarScripts) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // 获取本地脚本实体
                val localScript = bazaarScriptsRepository.getBazaarScriptsById(script.id ?: "")
                if (localScript != null) {
                    bazaarScriptsRepository.deleteBazaarScripts(localScript)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "删除成功"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "删除失败"
                )
            }
        }
    }

    // 选择脚本进行预览或查看详情
    fun selectScript(script: BazaarScripts) {
        _selectedScript.value = script
        _uiState.value = _uiState.value.copy(showScriptDetails = true)
    }

    // 关闭脚本详情
    fun closeScriptDetails() {
        _selectedScript.value = null
        _uiState.value = _uiState.value.copy(showScriptDetails = false)
    }

    // 关闭更新对话框
    fun dismissUpdateDialog() {
        _uiState.value = _uiState.value.copy(
            showUpdateDialog = false,
            scriptToUpdate = null
        )
    }

    // 清除消息
    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    // 清除错误
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

// UI状态数据类
data class BazaarScriptsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val showScriptDetails: Boolean = false,
    val showUpdateDialog: Boolean = false,
    val scriptToUpdate: BazaarScripts? = null
)