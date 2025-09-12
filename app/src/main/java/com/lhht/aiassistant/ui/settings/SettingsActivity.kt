package com.lhht.aiassistant.ui.settings

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.lhht.aiassistant.R
import com.lhht.aiassistant.databinding.ActivitySettingsBinding
import com.lhht.aiassistant.ui.config.ConfigSelectorActivity

/**
 * 设置Activity
 */
class SettingsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySettingsBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupClickListeners()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.settings)
    }
    
    private fun setupClickListeners() {
        binding.difyConfigItem.setOnClickListener {
            val intent = Intent(this, ConfigSelectorActivity::class.java).apply {
                putExtra("config_type", "dify")
            }
            startActivity(intent)
        }
        
        binding.xiaozhiConfigItem.setOnClickListener {
            val intent = Intent(this, ConfigSelectorActivity::class.java).apply {
                putExtra("config_type", "xiaozhi")
            }
            startActivity(intent)
        }
        
        binding.themeItem.setOnClickListener {
            val intent = Intent(this, ThemeSettingsActivity::class.java)
            startActivity(intent)
        }
        
        binding.aboutItem.setOnClickListener {
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
