package com.lhht.aiassistant.ui.settings

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.lhht.aiassistant.R
import com.lhht.aiassistant.databinding.ActivityThemeSettingsBinding

/**
 * 主题设置Activity
 */
class ThemeSettingsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityThemeSettingsBinding
    private lateinit var sharedPreferences: SharedPreferences
    
    companion object {
        private const val PREFS_NAME = "theme_prefs"
        private const val KEY_THEME_MODE = "theme_mode"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityThemeSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        setupToolbar()
        setupClickListeners()
        loadCurrentTheme()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.theme_settings)
    }
    
    private fun setupClickListeners() {
        binding.systemThemeCard.setOnClickListener {
            applyTheme(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
        
        binding.lightThemeCard.setOnClickListener {
            applyTheme(AppCompatDelegate.MODE_NIGHT_NO)
        }
        
        binding.darkThemeCard.setOnClickListener {
            applyTheme(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }
    
    private fun loadCurrentTheme() {
        val currentTheme = sharedPreferences.getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        
        // 重置所有选中状态
        binding.systemThemeCheckbox.isChecked = false
        binding.lightThemeCheckbox.isChecked = false
        binding.darkThemeCheckbox.isChecked = false
        
        // 设置当前主题为选中状态
        when (currentTheme) {
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> {
                binding.systemThemeCheckbox.isChecked = true
            }
            AppCompatDelegate.MODE_NIGHT_NO -> {
                binding.lightThemeCheckbox.isChecked = true
            }
            AppCompatDelegate.MODE_NIGHT_YES -> {
                binding.darkThemeCheckbox.isChecked = true
            }
        }
    }
    
    private fun applyTheme(themeMode: Int) {
        // 保存主题设置
        sharedPreferences.edit()
            .putInt(KEY_THEME_MODE, themeMode)
            .apply()
        
        // 应用主题
        AppCompatDelegate.setDefaultNightMode(themeMode)
        
        // 更新UI
        loadCurrentTheme()
        
        // 显示提示
        val themeName = when (themeMode) {
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> "跟随系统"
            AppCompatDelegate.MODE_NIGHT_NO -> "浅色主题"
            AppCompatDelegate.MODE_NIGHT_YES -> "深色主题"
            else -> "未知主题"
        }
        Toast.makeText(this, "已切换到$themeName", Toast.LENGTH_SHORT).show()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
