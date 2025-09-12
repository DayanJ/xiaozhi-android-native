package com.lhht.aiassistant.ui.voice

import android.Manifest
import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.lhht.aiassistant.R
import com.lhht.aiassistant.databinding.ActivityVoiceCallBinding
import com.lhht.aiassistant.service.XiaozhiService
import com.lhht.aiassistant.service.XiaozhiServiceEvent
import com.lhht.aiassistant.service.XiaozhiServiceEventType
import com.lhht.aiassistant.service.KeywordWakeupService
import com.lhht.aiassistant.viewmodel.ConfigViewModel
import com.lhht.aiassistant.viewmodel.ConversationViewModel
import com.lhht.aiassistant.model.MessageRole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 语音通话Activity
 */
class VoiceCallActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityVoiceCallBinding
    private lateinit var configViewModel: ConfigViewModel
    private lateinit var conversationViewModel: ConversationViewModel
    private var xiaozhiService: XiaozhiService? = null
    private var keywordWakeupService: KeywordWakeupService? = null
    
    private var conversationId: String? = null
    private var conversationTitle: String? = null
    private var configId: String? = null
    private var lastSttResult: String? = null // 跟踪最后的STT结果，避免重复添加
    
    private val scope = CoroutineScope(Dispatchers.Main)
    private var audioManager: AudioManager? = null
    private var isUserChangingVolume = false // 标记是否用户正在调节音量
    
    // 语音流状态
    private var isVoiceStreaming = false
    
    companion object {
        private const val PERMISSION_REQUEST_CODE = 1001
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVoiceCallBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // 获取传递的参数
        conversationId = intent.getStringExtra("conversation_id")
        conversationTitle = intent.getStringExtra("conversation_title")
        configId = intent.getStringExtra("config_id")
        
        setupToolbar()
        setupClickListeners()
        
        configViewModel = ViewModelProvider(this)[ConfigViewModel::class.java]
        conversationViewModel = ViewModelProvider(this)[ConversationViewModel::class.java]
        
        // 检查权限
        checkPermissions()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = conversationTitle ?: "语音通话"
    }
    
    private fun setupClickListeners() {
        binding.muteButton.setOnClickListener {
            xiaozhiService?.toggleMute()
            updateMuteButton()
        }
        
        // 语音流控制按钮 - 按住说话
        binding.voiceStreamButton.setOnTouchListener { _, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    startVoiceStreaming()
                    true
                }
                android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                    stopVoiceStreaming()
                    true
                }
                else -> false
            }
        }
        
        // 初始化AudioManager
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        
        // 设置音量控制
        binding.volumeSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    isUserChangingVolume = true
                    val volume = progress / 100.0f
                    xiaozhiService?.audioUtil?.setVolume(volume)
                    binding.volumeText.text = "${progress}%"
                }
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isUserChangingVolume = true
            }
            
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isUserChangingVolume = false
            }
        })
        
        // 初始化音量显示
        updateVolumeDisplay()
        
        binding.endCallButton.setOnClickListener {
            endCall()
        }
    }
    
    private fun checkPermissions() {
        val permissions = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.MODIFY_AUDIO_SETTINGS
        )
        
        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != android.content.pm.PackageManager.PERMISSION_GRANTED
        }
        
        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                missingPermissions.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        } else {
            initializeVoiceCall()
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            val allGranted = grantResults.all { it == android.content.pm.PackageManager.PERMISSION_GRANTED }
            
            if (allGranted) {
                initializeVoiceCall()
            } else {
                Toast.makeText(this, "需要麦克风权限才能进行语音通话", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
    
    private fun initializeVoiceCall() {
        scope.launch {
            try {
                // 获取小智配置
                var xiaozhiConfig = if (configId != null && configId!!.isNotEmpty()) {
                    configViewModel.getXiaozhiConfigById(configId!!)
                } else {
                    // 如果没有指定configId，获取第一个可用的小智配置
                    configViewModel.getFirstXiaozhiConfig()
                }
                
                if (xiaozhiConfig == null) {
                    Toast.makeText(this@VoiceCallActivity, "未找到小智配置，请先在设置中添加小智配置", Toast.LENGTH_LONG).show()
                    finish()
                    return@launch
                }
                
                // 初始化小智服务
                xiaozhiService = XiaozhiService.getInstance(
                    this@VoiceCallActivity,
                    xiaozhiConfig.websocketUrl,
                    xiaozhiConfig.macAddress,
                    xiaozhiConfig.token
                )
                
                // 初始化关键词唤醒服务
                keywordWakeupService = KeywordWakeupService(this@VoiceCallActivity)
                try {
                    keywordWakeupService?.initialize()
                    Log.d("VoiceCallActivity", "Keyword wakeup service initialized")
                } catch (e: Exception) {
                    Log.e("VoiceCallActivity", "Failed to initialize keyword wakeup service", e)
                    Toast.makeText(this@VoiceCallActivity, "语音唤醒功能初始化失败", Toast.LENGTH_SHORT).show()
                }
                
                // 添加事件监听器
                xiaozhiService?.addListener(::onXiaozhiServiceEvent)
                
                // 设置语音识别回调
                xiaozhiService?.setVoiceRecognitionCallback { recognizedText ->
                    // 将语音识别结果添加到消息列表
                    conversationId?.let { id ->
                        conversationViewModel.addMessage(
                            conversationId = id,
                            role = MessageRole.USER,
                            content = recognizedText
                        )
                    }
                }
                
                // 检查连接状态并连接
                if (xiaozhiService?.isConnected() != true) {
                    Log.d("VoiceCallActivity", "Not connected, attempting to connect...")
                    xiaozhiService?.connect()
                } else {
                    Log.d("VoiceCallActivity", "Already connected, reusing connection")
                }
                
                // 切换到语音通话模式
                xiaozhiService?.switchToVoiceCallMode()
                
                // 启动语音唤醒监听
                startKeywordWakeup()
                
                // 延迟更新连接状态，给连接时间建立
                delay(1000)
                updateConnectionStatus()
                
            } catch (e: Exception) {
                Toast.makeText(this@VoiceCallActivity, "初始化语音通话失败: ${e.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
    
    private fun onXiaozhiServiceEvent(event: XiaozhiServiceEvent) {
        // 确保UI更新在主线程中执行
        runOnUiThread {
            when (event.type) {
                XiaozhiServiceEventType.CONNECTED -> {
                    updateConnectionStatus()
                }
                XiaozhiServiceEventType.DISCONNECTED -> {
                    updateConnectionStatus()
                }
                XiaozhiServiceEventType.TEXT_MESSAGE -> {
                    // 只将AI回复添加到消息列表（避免重复显示）
                    val message = event.data as? String
                    if (message != null) {
                        // 将AI回复添加到消息列表
                        conversationId?.let { id ->
                            conversationViewModel.addMessage(
                                conversationId = id,
                                role = MessageRole.ASSISTANT,
                                content = message
                            )
                        }
                    }
                }
                XiaozhiServiceEventType.STT_RESULT -> {
                    // 显示语音识别结果
                    val recognizedText = event.data as? String
                    if (recognizedText != null) {
                        binding.statusText.text = "识别结果: $recognizedText"
                        
                        // 只有当STT结果与上次不同时才添加到消息列表（避免重复添加）
                        if (recognizedText != lastSttResult) {
                            lastSttResult = recognizedText
                            conversationId?.let { id ->
                                conversationViewModel.addMessage(
                                    conversationId = id,
                                    role = MessageRole.USER,
                                    content = recognizedText
                                )
                            }
                        }
                    }
                }
                XiaozhiServiceEventType.STT_STARTED -> {
                    binding.statusText.text = "正在识别语音..."
                    lastSttResult = null // 重置STT结果，准备新的识别会话
                }
                XiaozhiServiceEventType.STT_STOPPED -> {
                    binding.statusText.text = "语音识别完成"
                }
                XiaozhiServiceEventType.ERROR -> {
                    val error = event.data as? String
                    if (error != null) {
                        Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                    }
                }
                else -> {}
            }
        }
    }
    
    private fun updateConnectionStatus() {
        val isConnected = xiaozhiService?.isConnected() ?: false
        binding.connectionStatus.text = if (isConnected) "已连接" else "未连接"
        binding.connectionIndicator.setBackgroundColor(
            ContextCompat.getColor(this, if (isConnected) R.color.connected else R.color.disconnected)
        )
        
        // 更新语音流按钮状态
        updateVoiceStreamButton()
    }
    
    private fun updateMuteButton() {
        val isMuted = xiaozhiService?.isMuted() ?: false
        binding.muteButton.text = if (isMuted) "取消静音" else "静音"
    }
    
    /**
     * 开始语音流发送
     */
    private fun startVoiceStreaming() {
        if (isVoiceStreaming) return
        
        scope.launch {
            try {
                xiaozhiService?.startVoiceStreaming()
                isVoiceStreaming = true
                updateVoiceStreamButton()
                binding.statusText.text = "正在录音..."
            } catch (e: Exception) {
                Toast.makeText(this@VoiceCallActivity, "开始录音失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    /**
     * 停止语音流发送
     */
    private fun stopVoiceStreaming() {
        if (!isVoiceStreaming) return
        
        scope.launch {
            try {
                xiaozhiService?.stopVoiceStreaming()
                isVoiceStreaming = false
                updateVoiceStreamButton()
                binding.statusText.text = "录音已停止"
            } catch (e: Exception) {
                Toast.makeText(this@VoiceCallActivity, "停止录音失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    /**
     * 更新语音流按钮状态
     */
    private fun updateVoiceStreamButton() {
        binding.voiceStreamButton.text = if (isVoiceStreaming) "松开结束" else "按住说话"
        binding.voiceStreamButton.isEnabled = xiaozhiService?.isConnected() ?: false
    }
    
    private fun endCall() {
        scope.launch {
            try {
                // 停止语音流
                if (isVoiceStreaming) {
                    stopVoiceStreaming()
                }
                
                xiaozhiService?.disconnectVoiceCall()
                xiaozhiService?.switchToChatMode()
            } catch (e: Exception) {
                // 忽略错误
            }
        }
        finish()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        endCall()
        return true
    }
    
    override fun onResume() {
        super.onResume()
        // 界面重新显示时刷新连接状态
        updateConnectionStatus()
        // 更新音量显示
        updateVolumeDisplay()
    }
    
    
    /**
     * 更新音量显示
     */
    private fun updateVolumeDisplay() {
        try {
            val currentVolume = xiaozhiService?.audioUtil?.getVolume() ?: 0.8f
            val volumePercent = (currentVolume * 100).toInt()
            
            if (!isUserChangingVolume) {
                binding.volumeSeekbar.progress = volumePercent
                binding.volumeText.text = "${volumePercent}%"
            }
        } catch (e: Exception) {
            Log.e("VoiceCallActivity", "Failed to update volume display", e)
        }
    }
    
    /**
     * 启动关键词唤醒监听
     */
    private fun startKeywordWakeup() {
        try {
            keywordWakeupService?.startListening { keyword ->
                Log.d("VoiceCallActivity", "Keyword detected: $keyword")
                
                // 在UI线程中处理唤醒事件
                runOnUiThread {
                    binding.statusText.text = "检测到唤醒词: $keyword，开始录音..."
                    
                    // 自动开始语音流录制
                    if (!isVoiceStreaming) {
                        startVoiceStreaming()
                    }
                }
            }
            
            Log.d("VoiceCallActivity", "Keyword wakeup started")
            
        } catch (e: Exception) {
            Log.e("VoiceCallActivity", "Failed to start keyword wakeup", e)
            Toast.makeText(this, "启动语音唤醒失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * 停止关键词唤醒监听
     */
    private fun stopKeywordWakeup() {
        try {
            keywordWakeupService?.stopListening()
            Log.d("VoiceCallActivity", "Keyword wakeup stopped")
        } catch (e: Exception) {
            Log.e("VoiceCallActivity", "Failed to stop keyword wakeup", e)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        scope.launch {
            // 停止语音唤醒
            stopKeywordWakeup()
            
            // 释放关键词唤醒服务
            keywordWakeupService?.release()
            
            // 重置连接状态而不是完全释放资源
            xiaozhiService?.resetConnectionState()
        }
    }
}
