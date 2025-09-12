package com.lhht.aiassistant.ui.config

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.lhht.aiassistant.R
import com.lhht.aiassistant.databinding.ActivityConfigEditBinding
import com.lhht.aiassistant.model.DifyConfig
import com.lhht.aiassistant.model.XiaozhiConfig
import com.lhht.aiassistant.viewmodel.ConfigViewModel
import com.lhht.aiassistant.utils.DeviceUtil
import java.util.*

/**
 * 配置编辑Activity
 */
class ConfigEditActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityConfigEditBinding
    private val configViewModel: ConfigViewModel by viewModels()
    
    private var configType: String? = null
    private var configId: String? = null
    private var isEditing = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfigEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // 获取传递的参数
        configType = intent.getStringExtra("config_type")
        configId = intent.getStringExtra("config_id")
        isEditing = configId != null
        
        android.util.Log.d("ConfigEdit", "ConfigEditActivity初始化")
        android.util.Log.d("ConfigEdit", "configType: $configType")
        android.util.Log.d("ConfigEdit", "configId: $configId")
        android.util.Log.d("ConfigEdit", "isEditing: $isEditing")
        
        setupToolbar()
        setupViews()
        setupClickListeners()
        
        if (isEditing) {
            observeConfigData()
        }
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = if (isEditing) "编辑配置" else "添加配置"
    }
    
    private fun setupViews() {
        when (configType) {
            "dify" -> {
                binding.apiUrlLayout.visibility = android.view.View.VISIBLE
                binding.apiKeyLayout.visibility = android.view.View.VISIBLE
                binding.websocketUrlLayout.visibility = android.view.View.GONE
                binding.macAddressLayout.visibility = android.view.View.GONE
                binding.tokenLayout.visibility = android.view.View.GONE
            }
            "xiaozhi" -> {
                binding.apiUrlLayout.visibility = android.view.View.GONE
                binding.apiKeyLayout.visibility = android.view.View.GONE
                binding.websocketUrlLayout.visibility = android.view.View.VISIBLE
                binding.macAddressLayout.visibility = android.view.View.VISIBLE
                binding.tokenLayout.visibility = android.view.View.VISIBLE
                
                // 如果不是编辑模式，自动获取并填充MAC地址
                if (!isEditing) {
                    val macAddress = DeviceUtil.getMacAddress(this)
                    binding.macAddressEditText.setText(macAddress)
                    android.util.Log.d("ConfigEdit", "自动获取MAC地址: $macAddress")
                }
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.saveButton.setOnClickListener {
            android.util.Log.d("ConfigEdit", "保存按钮被点击")
            saveConfig()
        }
        
        binding.cancelButton.setOnClickListener {
            android.util.Log.d("ConfigEdit", "取消按钮被点击")
            finish()
        }
    }
    
    private fun observeConfigData() {
        when (configType) {
            "dify" -> {
                configViewModel.difyConfigs.observe(this) { configs ->
                    val config = configs.find { it.id == configId }
                    if (config != null) {
                        binding.nameEditText.setText(config.name)
                        binding.apiUrlEditText.setText(config.apiUrl)
                        binding.apiKeyEditText.setText(config.apiKey)
                    }
                }
            }
            "xiaozhi" -> {
                configViewModel.xiaozhiConfigs.observe(this) { configs ->
                    val config = configs.find { it.id == configId }
                    if (config != null) {
                        binding.nameEditText.setText(config.name)
                        binding.websocketUrlEditText.setText(config.websocketUrl)
                        binding.macAddressEditText.setText(config.macAddress)
                        binding.tokenEditText.setText(config.token)
                    }
                }
            }
        }
    }
    
    private fun saveConfig() {
        android.util.Log.d("ConfigEdit", "开始保存配置，类型: $configType")
        val name = binding.nameEditText.text.toString().trim()
        if (name.isEmpty()) {
            android.util.Log.w("ConfigEdit", "配置名称为空")
            Toast.makeText(this, "请输入配置名称", Toast.LENGTH_SHORT).show()
            return
        }
        
        android.util.Log.d("ConfigEdit", "配置名称: $name")
        when (configType) {
            "dify" -> {
                val apiUrl = binding.apiUrlEditText.text.toString().trim()
                val apiKey = binding.apiKeyEditText.text.toString().trim()
                
                if (apiUrl.isEmpty() || apiKey.isEmpty()) {
                    Toast.makeText(this, "请填写完整的Dify配置信息", Toast.LENGTH_SHORT).show()
                    return
                }
                
                val config = DifyConfig(
                    id = configId ?: UUID.randomUUID().toString(),
                    name = name,
                    apiUrl = apiUrl,
                    apiKey = apiKey
                )
                
                if (isEditing) {
                    configViewModel.updateDifyConfig(config)
                } else {
                    configViewModel.addDifyConfig(config)
                }
            }
            "xiaozhi" -> {
                android.util.Log.d("ConfigEdit", "处理小智配置")
                val websocketUrl = binding.websocketUrlEditText.text.toString().trim()
                val macAddress = binding.macAddressEditText.text.toString().trim()
                val token = binding.tokenEditText.text.toString().trim()
                
                android.util.Log.d("ConfigEdit", "WebSocket URL: $websocketUrl")
                android.util.Log.d("ConfigEdit", "MAC地址: $macAddress")
                android.util.Log.d("ConfigEdit", "Token: $token")
                
                if (websocketUrl.isEmpty() || macAddress.isEmpty() || token.isEmpty()) {
                    android.util.Log.w("ConfigEdit", "小智配置信息不完整")
                    Toast.makeText(this, "请填写完整的小智配置信息", Toast.LENGTH_SHORT).show()
                    return
                }
                
                val newConfigId = configId ?: UUID.randomUUID().toString()
                android.util.Log.d("ConfigEdit", "生成的配置ID: $newConfigId")
                android.util.Log.d("ConfigEdit", "原始configId: $configId")
                
                val config = XiaozhiConfig(
                    id = newConfigId,
                    name = name,
                    websocketUrl = websocketUrl,
                    macAddress = macAddress,
                    token = token
                )
                
                android.util.Log.d("ConfigEdit", "创建小智配置: $config")
                android.util.Log.d("ConfigEdit", "isEditing: $isEditing")
                if (isEditing) {
                    android.util.Log.d("ConfigEdit", "更新小智配置")
                    configViewModel.updateXiaozhiConfig(config)
                } else {
                    android.util.Log.d("ConfigEdit", "添加小智配置")
                    configViewModel.addXiaozhiConfig(config)
                }
            }
        }
        
        Toast.makeText(this, "配置保存成功", Toast.LENGTH_SHORT).show()
        finish()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
