package com.lhht.aiassistant.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lhht.aiassistant.model.DifyConfig

/**
 * Dify配置数据库实体
 */
@Entity(tableName = "dify_configs")
data class DifyConfigEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val apiUrl: String,
    val apiKey: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * 转换为业务模型
     */
    fun toDifyConfig(): DifyConfig {
        return DifyConfig(
            id = id,
            name = name,
            apiUrl = apiUrl,
            apiKey = apiKey
        )
    }
    
    companion object {
        /**
         * 从业务模型创建实体
         */
        fun fromDifyConfig(config: DifyConfig): DifyConfigEntity {
            return DifyConfigEntity(
                id = config.id,
                name = config.name,
                apiUrl = config.apiUrl,
                apiKey = config.apiKey,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        }
    }
}
