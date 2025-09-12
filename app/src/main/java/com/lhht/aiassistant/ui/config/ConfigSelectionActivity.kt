package com.lhht.aiassistant.ui.config

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.lhht.aiassistant.R
import com.lhht.aiassistant.databinding.ActivityConfigSelectionBinding
import com.lhht.aiassistant.model.XiaozhiConfig
import com.lhht.aiassistant.viewmodel.ConfigViewModel

/**
 * 配置选择Activity（用于新建对话时选择小智配置）
 */
class ConfigSelectionActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityConfigSelectionBinding
    private val configViewModel: ConfigViewModel by viewModels()
    private lateinit var configAdapter: ConfigSelectionAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfigSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupRecyclerView()
        observeViewModel()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "选择小智配置"
    }
    
    private fun setupRecyclerView() {
        configAdapter = ConfigSelectionAdapter { config ->
            // 选择配置并返回结果
            selectConfig(config)
        }
        
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ConfigSelectionActivity)
            adapter = configAdapter
        }
    }
    
    private fun observeViewModel() {
        configViewModel.xiaozhiConfigs.observe(this, Observer { configs ->
            configAdapter.submitList(configs)
            
            if (configs.isEmpty()) {
                binding.emptyState.visibility = android.view.View.VISIBLE
                binding.recyclerView.visibility = android.view.View.GONE
                binding.emptyStateText.text = "没有小智配置\n请先在设置中添加小智配置"
            } else {
                binding.emptyState.visibility = android.view.View.GONE
                binding.recyclerView.visibility = android.view.View.VISIBLE
            }
        })
        
        configViewModel.error.observe(this, Observer { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                configViewModel.clearError()
            }
        })
    }
    
    private fun selectConfig(config: XiaozhiConfig) {
        val resultIntent = Intent().apply {
            putExtra("selected_config_id", config.id)
            putExtra("selected_config_name", config.name)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        setResult(RESULT_CANCELED)
        finish()
        return true
    }
    
    override fun onBackPressed() {
        setResult(RESULT_CANCELED)
        super.onBackPressed()
    }
}
