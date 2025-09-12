package com.lhht.aiassistant.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.lhht.aiassistant.R
import com.lhht.aiassistant.databinding.ActivityAboutBinding

/**
 * 关于页面Activity
 */
class AboutActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityAboutBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupViews()
        setupClickListeners()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.about)
    }
    
    private fun setupViews() {
        // 设置应用版本信息
        binding.appVersionText.text = "版本 1.0.0 (1)"
        
        // 设置应用描述
        binding.appDescriptionText.text = """
            小智是一个智能语音助手应用，支持多种AI服务：
            
            • Dify对话：基于HTTP API的文本对话，支持图片上传
            • 小智对话：基于WebSocket的语音对话，支持实时语音交互
            • 语音唤醒：本地关键词识别，支持"小安小安"唤醒
            
            应用采用Android原生开发，使用MVVM架构模式，
            集成了Opus音频编解码和SherpaOnnx语音识别技术，
            提供流畅的语音交互体验。
            
            这是xiaozhi-android-native开源项目。
        """.trimIndent()
    }
    
    private fun setupClickListeners() {
        binding.githubCard.setOnClickListener {
            openUrl("https://github.com/your-username/xiaozhi-android-native")
        }
        
        binding.emailCard.setOnClickListener {
            sendEmail()
        }
        
        binding.licenseCard.setOnClickListener {
            showLicense()
        }
        
        binding.privacyCard.setOnClickListener {
            showPrivacyPolicy()
        }
    }
    
    private fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "无法打开链接", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun sendEmail() {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf("jingdayanw@gmail.com"))
                putExtra(Intent.EXTRA_SUBJECT, "小智应用反馈")
                putExtra(Intent.EXTRA_TEXT, "请在此处输入您的反馈内容...")
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "无法打开邮件应用", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun showLicense() {
        Toast.makeText(this, "开源许可证信息", Toast.LENGTH_SHORT).show()
        // 这里可以打开许可证详情页面
    }
    
    private fun showPrivacyPolicy() {
        Toast.makeText(this, "隐私政策", Toast.LENGTH_SHORT).show()
        // 这里可以打开隐私政策页面
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
