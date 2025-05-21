package com.vlog.my.data.bazaar

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID
import kotlin.String

/**
 * BazaarScripts配置数据模型，用于存储用户配置的BazaarScripts信息
 */
@Entity(tableName = "bazaar_scripts")
data class BazaarScriptsEntity(
    @PrimaryKey
    var id: String = UUID.randomUUID().toString(),
    var createdAt: Long? = null,
    var isLocked: Int? = null,
    var isEnabled: Int? = null,
    var isTyped: Int? = null,
    var isValued: Int? = null,
    var isCommented: Int? = null,
    var isRecommend: Int? = null,
    var version: Int = 0,
    var createdBy: String? = null,
    var attachmentId: String? = null,
    var title: String? = null,
    var description: String? = null,
    var tags: String? = null,
    var configTyped: Int? = null,
    var configs: String? = null
) {


    fun toBazaarScripts(): BazaarScripts {
        return BazaarScripts(
            id = id,
            createdAt = createdAt,
            isLocked = isLocked,
            isEnabled = isEnabled,
            isTyped = isTyped,
            isValued = isValued,
            isCommented = isCommented,
            isRecommend = isRecommend,
            version = version,
            createdBy = createdBy,
            attachmentId = attachmentId,
            title = title,
            description = description,
            tags = tags,
            configTyped = configTyped,
            configs = configs
        )
    }

    companion object {
        /**
         * 从Category模型创建实体
         */
        fun fromBazaarScripts(bazaarScripts: BazaarScripts): BazaarScriptsEntity {
            return BazaarScriptsEntity(
                id = bazaarScripts.id!!,
                createdAt = bazaarScripts.createdAt,
                isLocked = bazaarScripts.isLocked,
                isEnabled = bazaarScripts.isEnabled,
                isTyped = bazaarScripts.isTyped,
                isValued = bazaarScripts.isValued,
                isCommented = bazaarScripts.isCommented,
                isRecommend = bazaarScripts.isRecommend,
                version = bazaarScripts.version,
                createdBy = bazaarScripts.createdBy,
                attachmentId = bazaarScripts.attachmentId,
                title = bazaarScripts.title,
                description = bazaarScripts.description,
                tags = bazaarScripts.tags,
                configTyped = bazaarScripts.configTyped,
                configs = bazaarScripts.configs
            )
        }
    }
}