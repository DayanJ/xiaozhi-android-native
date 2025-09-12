package com.lhht.aiassistant.ui.config

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.lhht.aiassistant.R
import com.lhht.aiassistant.databinding.ActivityConfigSelectorBinding
import com.lhht.aiassistant.model.DifyConfig
import com.lhht.aiassistant.model.XiaozhiConfig
import com.lhht.aiassistant.viewmodel.ConfigViewModel

/**
 * 配置选择Activity
 */
class ConfigSelectorActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityConfigSelectorBinding
    private val configViewModel: ConfigViewModel by viewModels()
    private lateinit var configAdapter: ConfigAdapter
    
    private var configType: String? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfigSelectorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // 获取配置类型
        configType = intent.getStringExtra("config_type")
        
        setupToolbar()
        setupRecyclerView()
        setupFloatingActionButton()
        observeViewModel()
    }
    
    override fun onResume() {
        super.onResume()
        // 从编辑页面返回时刷新配置列表
        android.util.Log.d("ConfigSelector", "onResume: 刷新配置列表")
        refreshConfigs()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = when (configType) {
            "dify" -> "Dify配置"
            "xiaozhi" -> "小智配置"
            else -> "配置管理"
        }
    }
    
    private fun setupRecyclerView() {
        configAdapter = ConfigAdapter(
            onConfigClick = { config ->
                // 点击配置项（暂时不处理，或者可以显示详情）
                android.util.Log.d("ConfigSelector", "点击配置: $config")
            },
            onConfigEdit = { config ->
                // 编辑配置
                editConfig(config)
            },
            onConfigDelete = { config ->
                // 删除配置
                deleteConfig(config)
            }
        )
        
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ConfigSelectorActivity)
            adapter = configAdapter
        }
    }
    
    private fun setupFloatingActionButton() {
        binding.fab.setOnClickListener {
            addConfig()
        }
    }
    
    private fun observeViewModel() {
        try {
            when (configType) {
                "dify" -> {
                    configViewModel.difyConfigs.observe(this, Observer { configs ->
                        try {
                            configAdapter.submitList(configs)
                            
                            if (configs.isEmpty()) {
                                binding.emptyState.visibility = android.view.View.VISIBLE
                                binding.recyclerView.visibility = android.view.View.GONE
                            } else {
                                binding.emptyState.visibility = android.view.View.GONE
                                binding.recyclerView.visibility = android.view.View.VISIBLE
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("ConfigSelector", "处理Dify配置时发生错误", e)
                            Toast.makeText(this, "显示配置时发生错误", Toast.LENGTH_SHORT).show()
                        }
                    })
                }
                "xiaozhi" -> {
                    configViewModel.xiaozhiConfigs.observe(this, Observer { configs ->
                        try {
                            android.util.Log.d("ConfigSelector", "收到小智配置更新，数量: ${configs.size}")
                            configAdapter.submitList(configs)
                            android.util.Log.d("ConfigSelector", "适配器已更新")
                            
                            if (configs.isEmpty()) {
                                android.util.Log.d("ConfigSelector", "显示空状态")
                                binding.emptyState.visibility = android.view.View.VISIBLE
                                binding.recyclerView.visibility = android.view.View.GONE
                            } else {
                                android.util.Log.d("ConfigSelector", "显示配置列表")
                                binding.emptyState.visibility = android.view.View.GONE
                                binding.recyclerView.visibility = android.view.View.VISIBLE
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("ConfigSelector", "处理小智配置时发生错误", e)
                            Toast.makeText(this, "显示配置时发生错误", Toast.LENGTH_SHORT).show()
                        }
                    })
                }
                else -> {
                    Toast.makeText(this, "未知的配置类型: $configType", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            
            configViewModel.error.observe(this, Observer { error ->
                error?.let {
                    Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                    configViewModel.clearError()
                }
            })
            
            configViewModel.isLoading.observe(this, Observer { isLoading ->
                // 可以在这里显示加载指示器
                if (isLoading) {
                    android.util.Log.d("ConfigSelector", "正在加载配置...")
                }
            })
        } catch (e: Exception) {
            android.util.Log.e("ConfigSelector", "观察ViewModel时发生错误", e)
            Toast.makeText(this, "初始化配置页面时发生错误", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    
    private fun addConfig() {
        val intent = Intent(this, ConfigEditActivity::class.java).apply {
            putExtra("config_type", configType)
        }
        startActivity(intent)
    }
    
    private fun editConfig(config: Any) {
        val intent = Intent(this, ConfigEditActivity::class.java).apply {
            putExtra("config_type", configType)
            when (config) {
                is DifyConfig -> {
                    putExtra("config_id", config.id)
                    putExtra("config_name", config.name)
                    putExtra("api_url", config.apiUrl)
                    putExtra("api_key", config.apiKey)
                }
                is XiaozhiConfig -> {
                    putExtra("config_id", config.id)
                    putExtra("config_name", config.name)
                    putExtra("websocket_url", config.websocketUrl)
                    putExtra("mac_address", config.macAddress)
                    putExtra("token", config.token)
                }
            }
        }
        startActivity(intent)
    }
    
    /**
     * 删除配置
     */
    private fun deleteConfig(config: Any) {
        val configName = when (config) {
            is DifyConfig -> config.name
            is XiaozhiConfig -> config.name
            else -> "配置"
        }
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("确认删除")
            .setMessage("确定要删除配置 \"$configName\" 吗？")
            .setPositiveButton("删除") { _, _ ->
                when (config) {
                    is DifyConfig -> {
                        configViewModel.deleteDifyConfig(config)
                        Toast.makeText(this, "Dify配置已删除", Toast.LENGTH_SHORT).show()
                    }
                    is XiaozhiConfig -> {
                        configViewModel.deleteXiaozhiConfig(config)
                        Toast.makeText(this, "小智配置已删除", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    /**
     * 刷新配置列表
     */
    private fun refreshConfigs() {
        android.util.Log.d("ConfigSelector", "开始刷新配置列表，类型: $configType")
        // 由于使用Flow，数据会自动更新，这里只需要调用refreshConfigs方法
        configViewModel.refreshConfigs()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
