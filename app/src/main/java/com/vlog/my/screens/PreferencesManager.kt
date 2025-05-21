package com.vlog.my.screens

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.vlog.my.data.model.ScreensViewTyped
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 用户偏好设置管理器
 */
@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    /**
     * 保存小程序视图类型偏好
     * @param subScriptId 小程序ID
     * @param viewType 视图类型
     */
    fun saveSubScriptViewType(subScriptId: String, viewType: ScreensViewTyped) {
        sharedPreferences.edit {
            putString(getSubScriptViewTypeKey(subScriptId), viewType.name)
        }
    }

    /**
     * 获取小程序视图类型偏好
     * @param subScriptId 小程序ID
     * @return 视图类型，默认为列表视图
     */
    fun getSubScriptViewType(subScriptId: String): ScreensViewTyped {
        val viewTypeName = sharedPreferences.getString(getSubScriptViewTypeKey(subScriptId), ScreensViewTyped.LIST.name)
        return try {
            ScreensViewTyped.valueOf(viewTypeName ?: ScreensViewTyped.LIST.name)
        } catch (e: IllegalArgumentException) {
            ScreensViewTyped.LIST
        }
    }

    /**
     * 生成小程序视图类型偏好的键
     * @param subScriptId 小程序ID
     * @return 偏好键
     */
    private fun getSubScriptViewTypeKey(subScriptId: String): String {
        return "$KEY_SUBSCRIPT_VIEW_TYPE_PREFIX$subScriptId"
    }

    companion object {
        private const val PREFERENCES_NAME = "subscripts_preferences"
        private const val KEY_SUBSCRIPT_VIEW_TYPE_PREFIX = "subscript_view_type_"
    }
}