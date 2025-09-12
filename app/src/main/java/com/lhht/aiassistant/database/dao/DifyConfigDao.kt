package com.lhht.aiassistant.database.dao

import androidx.room.*
import com.lhht.aiassistant.database.entity.DifyConfigEntity
import kotlinx.coroutines.flow.Flow

/**
 * Dify配置数据访问对象
 */
@Dao
interface DifyConfigDao {
    
    @Query("SELECT * FROM dify_configs ORDER BY updatedAt DESC")
    fun getAllDifyConfigs(): Flow<List<DifyConfigEntity>>
    
    @Query("SELECT * FROM dify_configs WHERE id = :configId")
    suspend fun getDifyConfigById(configId: String): DifyConfigEntity?
    
    @Query("SELECT * FROM dify_configs WHERE name = :name")
    suspend fun getDifyConfigByName(name: String): DifyConfigEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDifyConfig(config: DifyConfigEntity)
    
    @Update
    suspend fun updateDifyConfig(config: DifyConfigEntity)
    
    @Delete
    suspend fun deleteDifyConfig(config: DifyConfigEntity)
    
    @Query("DELETE FROM dify_configs WHERE id = :configId")
    suspend fun deleteDifyConfigById(configId: String)
    
    @Query("DELETE FROM dify_configs")
    suspend fun deleteAllDifyConfigs()
    
    @Query("SELECT COUNT(*) FROM dify_configs")
    suspend fun getDifyConfigCount(): Int
}
