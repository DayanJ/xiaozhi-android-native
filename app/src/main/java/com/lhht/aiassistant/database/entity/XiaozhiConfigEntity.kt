package com.lhht.aiassistant.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lhht.aiassistant.model.XiaozhiConfig

/**
 * 小智配置数据库实体
 */
@Entity(tableName = "xiaozhi_configs")
data class XiaozhiConfigEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val websocketUrl: String,
    val macAddress: String,
    val token: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * 转换为业务模型
     */
    fun toXiaozhiConfig(): XiaozhiConfig {
        return XiaozhiConfig(
            id = id,
            name = name,
            websocketUrl = websocketUrl,
            macAddress = macAddress,
            token = token
        )
    }
    
    companion object {
        /**
         * 从业务模型创建实体
         */
        fun fromXiaozhiConfig(config: XiaozhiConfig): XiaozhiConfigEntity {
            return XiaozhiConfigEntity(
                id = config.id,
                name = config.name,
                websocketUrl = config.websocketUrl,
                macAddress = config.macAddress,
                token = config.token,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        }
    }
}
