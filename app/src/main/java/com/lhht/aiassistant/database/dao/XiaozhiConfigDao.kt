package com.lhht.aiassistant.database.dao

import androidx.room.*
import com.lhht.aiassistant.database.entity.XiaozhiConfigEntity
import kotlinx.coroutines.flow.Flow

/**
 * 小智配置数据访问对象
 */
@Dao
interface XiaozhiConfigDao {
    
    @Query("SELECT * FROM xiaozhi_configs ORDER BY updatedAt DESC")
    fun getAllXiaozhiConfigs(): Flow<List<XiaozhiConfigEntity>>
    
    @Query("SELECT * FROM xiaozhi_configs WHERE id = :configId")
    suspend fun getXiaozhiConfigById(configId: String): XiaozhiConfigEntity?
    
    @Query("SELECT * FROM xiaozhi_configs WHERE name = :name")
    suspend fun getXiaozhiConfigByName(name: String): XiaozhiConfigEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertXiaozhiConfig(config: XiaozhiConfigEntity)
    
    @Update
    suspend fun updateXiaozhiConfig(config: XiaozhiConfigEntity)
    
    @Delete
    suspend fun deleteXiaozhiConfig(config: XiaozhiConfigEntity)
    
    @Query("DELETE FROM xiaozhi_configs WHERE id = :configId")
    suspend fun deleteXiaozhiConfigById(configId: String)
    
    @Query("DELETE FROM xiaozhi_configs")
    suspend fun deleteAllXiaozhiConfigs()
    
    @Query("SELECT COUNT(*) FROM xiaozhi_configs")
    suspend fun getXiaozhiConfigCount(): Int
}
