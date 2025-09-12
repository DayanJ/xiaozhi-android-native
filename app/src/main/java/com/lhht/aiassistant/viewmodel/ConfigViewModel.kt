package com.lhht.aiassistant.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.asLiveData
import com.lhht.aiassistant.database.AppDatabase
import com.lhht.aiassistant.model.DifyConfig
import com.lhht.aiassistant.model.XiaozhiConfig
import com.lhht.aiassistant.repository.ConfigRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

/**
 * 配置ViewModel
 */
class ConfigViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getDatabase(application)
    private val configRepository = ConfigRepository(database)
    
    // 使用Flow转换为LiveData
    val difyConfigs: LiveData<List<DifyConfig>> = configRepository.getAllDifyConfigs().asLiveData()
    val xiaozhiConfigs: LiveData<List<XiaozhiConfig>> = configRepository.getAllXiaozhiConfigs().asLiveData()
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    init {
        // 由于使用Flow，不需要手动加载数据
        Log.d("ConfigViewModel", "ConfigViewModel初始化完成")
    }
    
    /**
     * 添加Dify配置
     */
    fun addDifyConfig(config: DifyConfig) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                configRepository.insertDifyConfig(config)
                Log.d("ConfigViewModel", "Dify配置已添加: ${config.name}")
            } catch (e: Exception) {
                _error.value = e.message
                Log.e("ConfigViewModel", "添加Dify配置失败", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 更新Dify配置
     */
    fun updateDifyConfig(config: DifyConfig) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                configRepository.updateDifyConfig(config)
                Log.d("ConfigViewModel", "Dify配置已更新: ${config.name}")
            } catch (e: Exception) {
                _error.value = e.message
                Log.e("ConfigViewModel", "更新Dify配置失败", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 删除Dify配置
     */
    fun deleteDifyConfig(config: DifyConfig) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                configRepository.deleteDifyConfig(config)
                Log.d("ConfigViewModel", "Dify配置已删除: ${config.name}")
            } catch (e: Exception) {
                _error.value = e.message
                Log.e("ConfigViewModel", "删除Dify配置失败", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 根据ID删除Dify配置
     */
    fun deleteDifyConfigById(configId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                configRepository.deleteDifyConfigById(configId)
                Log.d("ConfigViewModel", "Dify配置已删除: $configId")
            } catch (e: Exception) {
                _error.value = e.message
                Log.e("ConfigViewModel", "删除Dify配置失败", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 添加小智配置
     */
    fun addXiaozhiConfig(config: XiaozhiConfig) {
        Log.d("ConfigViewModel", "开始添加小智配置: $config")
        viewModelScope.launch {
            try {
                _isLoading.value = true
                configRepository.insertXiaozhiConfig(config)
                Log.d("ConfigViewModel", "小智配置已添加: ${config.name}")
            } catch (e: Exception) {
                Log.e("ConfigViewModel", "添加小智配置时发生错误", e)
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 更新小智配置
     */
    fun updateXiaozhiConfig(config: XiaozhiConfig) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                configRepository.updateXiaozhiConfig(config)
                Log.d("ConfigViewModel", "小智配置已更新: ${config.name}")
            } catch (e: Exception) {
                _error.value = e.message
                Log.e("ConfigViewModel", "更新小智配置失败", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 删除小智配置
     */
    fun deleteXiaozhiConfig(config: XiaozhiConfig) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                configRepository.deleteXiaozhiConfig(config)
                Log.d("ConfigViewModel", "小智配置已删除: ${config.name}")
            } catch (e: Exception) {
                _error.value = e.message
                Log.e("ConfigViewModel", "删除小智配置失败", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 根据ID删除小智配置
     */
    fun deleteXiaozhiConfigById(configId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                configRepository.deleteXiaozhiConfigById(configId)
                Log.d("ConfigViewModel", "小智配置已删除: $configId")
            } catch (e: Exception) {
                _error.value = e.message
                Log.e("ConfigViewModel", "删除小智配置失败", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 根据ID获取Dify配置
     */
    suspend fun getDifyConfigById(configId: String): DifyConfig? {
        return try {
            configRepository.getDifyConfigById(configId)
        } catch (e: Exception) {
            Log.e("ConfigViewModel", "获取Dify配置失败", e)
            _error.value = e.message
            null
        }
    }
    
    /**
     * 根据ID获取小智配置
     */
    suspend fun getXiaozhiConfigById(configId: String): XiaozhiConfig? {
        return try {
            configRepository.getXiaozhiConfigById(configId)
        } catch (e: Exception) {
            Log.e("ConfigViewModel", "获取小智配置失败", e)
            _error.value = e.message
            null
        }
    }
    
    /**
     * 获取第一个小智配置（用于默认选择）
     */
    suspend fun getFirstXiaozhiConfig(): XiaozhiConfig? {
        return try {
            configRepository.getFirstXiaozhiConfig()
        } catch (e: Exception) {
            Log.e("ConfigViewModel", "获取第一个小智配置失败", e)
            _error.value = e.message
            null
        }
    }
    
    /**
     * 清除错误信息
     */
    fun clearError() {
        _error.value = null
    }
    
    /**
     * 刷新配置（由于使用Flow，这个方法主要用于调试）
     */
    fun refreshConfigs() {
        Log.d("ConfigViewModel", "刷新配置 - 由于使用Flow，数据会自动更新")
    }
}