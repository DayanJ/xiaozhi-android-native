package com.lhht.aiassistant.repository

import com.lhht.aiassistant.database.AppDatabase
import com.lhht.aiassistant.database.entity.DifyConfigEntity
import com.lhht.aiassistant.database.entity.XiaozhiConfigEntity
import com.lhht.aiassistant.model.DifyConfig
import com.lhht.aiassistant.model.XiaozhiConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 配置数据仓库
 */
class ConfigRepository(private val database: AppDatabase) {
    
    private val difyConfigDao = database.difyConfigDao()
    private val xiaozhiConfigDao = database.xiaozhiConfigDao()
    
    // Dify配置相关操作
    fun getAllDifyConfigs(): Flow<List<DifyConfig>> {
        return difyConfigDao.getAllDifyConfigs().map { entities ->
            entities.map { it.toDifyConfig() }
        }
    }
    
    suspend fun getDifyConfigById(configId: String): DifyConfig? {
        return difyConfigDao.getDifyConfigById(configId)?.toDifyConfig()
    }
    
    suspend fun getDifyConfigByName(name: String): DifyConfig? {
        return difyConfigDao.getDifyConfigByName(name)?.toDifyConfig()
    }
    
    suspend fun insertDifyConfig(config: DifyConfig) {
        difyConfigDao.insertDifyConfig(DifyConfigEntity.fromDifyConfig(config))
    }
    
    suspend fun updateDifyConfig(config: DifyConfig) {
        val entity = DifyConfigEntity.fromDifyConfig(config)
        difyConfigDao.updateDifyConfig(entity)
    }
    
    suspend fun deleteDifyConfig(config: DifyConfig) {
        difyConfigDao.deleteDifyConfig(DifyConfigEntity.fromDifyConfig(config))
    }
    
    suspend fun deleteDifyConfigById(configId: String) {
        difyConfigDao.deleteDifyConfigById(configId)
    }
    
    suspend fun deleteAllDifyConfigs() {
        difyConfigDao.deleteAllDifyConfigs()
    }
    
    suspend fun getDifyConfigCount(): Int {
        return difyConfigDao.getDifyConfigCount()
    }
    
    // 小智配置相关操作
    fun getAllXiaozhiConfigs(): Flow<List<XiaozhiConfig>> {
        return xiaozhiConfigDao.getAllXiaozhiConfigs().map { entities ->
            entities.map { it.toXiaozhiConfig() }
        }
    }
    
    suspend fun getXiaozhiConfigById(configId: String): XiaozhiConfig? {
        return xiaozhiConfigDao.getXiaozhiConfigById(configId)?.toXiaozhiConfig()
    }
    
    suspend fun getXiaozhiConfigByName(name: String): XiaozhiConfig? {
        return xiaozhiConfigDao.getXiaozhiConfigByName(name)?.toXiaozhiConfig()
    }
    
    suspend fun insertXiaozhiConfig(config: XiaozhiConfig) {
        xiaozhiConfigDao.insertXiaozhiConfig(XiaozhiConfigEntity.fromXiaozhiConfig(config))
    }
    
    suspend fun updateXiaozhiConfig(config: XiaozhiConfig) {
        val entity = XiaozhiConfigEntity.fromXiaozhiConfig(config)
        xiaozhiConfigDao.updateXiaozhiConfig(entity)
    }
    
    suspend fun deleteXiaozhiConfig(config: XiaozhiConfig) {
        xiaozhiConfigDao.deleteXiaozhiConfig(XiaozhiConfigEntity.fromXiaozhiConfig(config))
    }
    
    suspend fun deleteXiaozhiConfigById(configId: String) {
        xiaozhiConfigDao.deleteXiaozhiConfigById(configId)
    }
    
    suspend fun deleteAllXiaozhiConfigs() {
        xiaozhiConfigDao.deleteAllXiaozhiConfigs()
    }
    
    suspend fun getXiaozhiConfigCount(): Int {
        return xiaozhiConfigDao.getXiaozhiConfigCount()
    }
    
    /**
     * 获取第一个小智配置（用于默认选择）
     */
    suspend fun getFirstXiaozhiConfig(): XiaozhiConfig? {
        return try {
            // 获取所有配置并返回第一个
            val allConfigs = xiaozhiConfigDao.getAllXiaozhiConfigs()
            var firstConfig: XiaozhiConfig? = null
            allConfigs.collect { entities ->
                firstConfig = entities.firstOrNull()?.toXiaozhiConfig()
            }
            firstConfig
        } catch (e: Exception) {
            null
        }
    }
}
