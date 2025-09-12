package com.lhht.aiassistant.ui.chat

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.lhht.aiassistant.R
import com.lhht.aiassistant.databinding.ActivityChatBinding
import com.lhht.aiassistant.model.ConversationType
import com.lhht.aiassistant.model.MessageRole
import com.lhht.aiassistant.service.DifyService
import com.lhht.aiassistant.service.XiaozhiService
import com.lhht.aiassistant.service.XiaozhiServiceEvent
import com.lhht.aiassistant.service.XiaozhiServiceEventType
import com.lhht.aiassistant.ui.voice.VoiceCallActivity
import com.lhht.aiassistant.viewmodel.ConversationViewModel
import com.lhht.aiassistant.viewmodel.ConfigViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

/**
 * 聊天Activity
 */
class ChatActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityChatBinding
    private val conversationViewModel: ConversationViewModel by viewModels()
    private val configViewModel: ConfigViewModel by viewModels()
    private lateinit var messageAdapter: MessageAdapter
    
    private var conversationId: String? = null
    private var conversationTitle: String? = null
    private var conversationType: ConversationType? = null
    private var configId: String? = null
    
    private var difyService: DifyService? = null
    private var xiaozhiService: XiaozhiService? = null
    
    private val scope = CoroutineScope(Dispatchers.Main)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // 获取传递的参数
        conversationId = intent.getStringExtra("conversation_id")
        conversationTitle = intent.getStringExtra("conversation_title")
        val typeString = intent.getStringExtra("conversation_type")
        conversationType = if (typeString != null) ConversationType.valueOf(typeString) else null
        configId = intent.getStringExtra("config_id")
        
        setupToolbar()
        setupRecyclerView()
        setupInputArea()
        observeViewModel()
        
        // 初始化服务
        initializeServices()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = conversationTitle ?: "聊天"
    }
    
    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter()
        
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity).apply {
                stackFromEnd = true
            }
            adapter = messageAdapter
        }
    }
    
    private fun setupInputArea() {
        binding.sendButton.setOnClickListener {
            sendMessage()
        }
        
        binding.inputEditText.setOnEditorActionListener { _, _, _ ->
            sendMessage()
            true
        }
    }
    
    private fun observeViewModel() {
        // 观察消息变化
        conversationId?.let { id ->
            conversationViewModel.getMessages(id).observe(this, Observer { messages: List<com.lhht.aiassistant.model.Message> ->
                messageAdapter.submitList(messages)
                
                // 滚动到底部
                if (messages.isNotEmpty()) {
                    binding.recyclerView.scrollToPosition(messages.size - 1)
                }
            })
        }
        
        conversationViewModel.error.observe(this, Observer { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                conversationViewModel.clearError()
            }
        })
        
        // 观察小智配置变化
        configViewModel.xiaozhiConfigs.observe(this, Observer { configs ->
            android.util.Log.d("ChatActivity", "收到小智配置更新，数量: ${configs.size}")
            if (conversationType == ConversationType.XIAOZHI && xiaozhiService == null) {
                initializeXiaozhiService(configs)
            }
        })
    }
    
    private fun initializeServices() {
        android.util.Log.d("ChatActivity", "初始化服务，对话类型: $conversationType, configId: $configId")
        conversationType?.let { type ->
            when (type) {
                ConversationType.DIFY -> {
                    android.util.Log.d("ChatActivity", "初始化Dify服务")
                    // 初始化Dify服务
                    // 这里需要从配置中获取API信息
                }
                ConversationType.XIAOZHI -> {
                    android.util.Log.d("ChatActivity", "小智服务将通过观察者模式初始化")
                    // 小智服务将通过观察者模式初始化
                    // 设置当前对话ID，确保消息路由到正确的对话
                    xiaozhiService?.setCurrentConversationId(conversationId)
                }
            }
        }
    }
    
    private fun sendMessage() {
        val message = binding.inputEditText.text.toString().trim()
        if (message.isEmpty()) return
        
        binding.inputEditText.text?.clear()
        
        // 添加用户消息
        conversationViewModel.addMessage(
            conversationId = conversationId ?: return,
            role = MessageRole.USER,
            content = message
        )
        
        // 发送到服务
        scope.launch {
            try {
                when (conversationType) {
                    ConversationType.DIFY -> {
                        // 发送到Dify服务
                        difyService?.let { service ->
                            val response = service.sendMessage(message, conversationId ?: "")
                            conversationViewModel.addMessage(
                                conversationId = conversationId ?: return@launch,
                                role = MessageRole.ASSISTANT,
                                content = response
                            )
                        }
                    }
                    ConversationType.XIAOZHI -> {
                        // 发送到小智服务
                        xiaozhiService?.let { service ->
                            // 设置当前对话ID，确保消息路由到正确的对话
                            service.setCurrentConversationId(conversationId)
                            
                            // 使用实时消息回调
                            service.sendTextMessage(message) { sentence ->
                                // 实时添加每个句子到消息列表
                                conversationViewModel.addMessage(
                                    conversationId = conversationId ?: return@sendTextMessage,
                                    role = MessageRole.ASSISTANT,
                                    content = sentence
                                )
                            }
                            // 实际消息已经通过回调实时添加了
                        }
                    }
                    null -> {
                        Toast.makeText(this@ChatActivity, "未知的对话类型", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                conversationViewModel.addMessage(
                    conversationId = conversationId ?: return@launch,
                    role = MessageRole.ASSISTANT,
                    content = "发生错误: ${e.message}"
                )
            }
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.chat_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_voice_call -> {
                if (conversationType == ConversationType.XIAOZHI) {
                    startVoiceCall()
                } else {
                    Toast.makeText(this, "语音通话仅适用于小智对话", Toast.LENGTH_SHORT).show()
                }
                true
            }
            R.id.action_new_conversation -> {
                if (conversationType == ConversationType.DIFY) {
                    // 开始新对话
                    scope.launch {
                        difyService?.clearConversation(conversationId ?: "")
                        conversationViewModel.addMessage(
                            conversationId = conversationId ?: return@launch,
                            role = MessageRole.SYSTEM,
                            content = "--- 开始新对话 ---"
                        )
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun startVoiceCall() {
        val intent = Intent(this, VoiceCallActivity::class.java).apply {
            putExtra("conversation_id", conversationId)
            putExtra("conversation_title", conversationTitle)
            putExtra("config_id", configId)
        }
        startActivity(intent)
    }
    
    /**
     * 处理小智服务事件
     */
    private fun onXiaozhiServiceEvent(event: XiaozhiServiceEvent) {
        when (event.type) {
            XiaozhiServiceEventType.CONNECTED -> {
                // 连接成功
                runOnUiThread {
                    Toast.makeText(this, "小智服务已连接", Toast.LENGTH_SHORT).show()
                }
            }
            XiaozhiServiceEventType.DISCONNECTED -> {
                // 连接断开
                runOnUiThread {
                    Toast.makeText(this, "小智服务已断开", Toast.LENGTH_SHORT).show()
                }
            }
            XiaozhiServiceEventType.TEXT_MESSAGE -> {
                // 收到文本消息
                // 注意：文本输入的回复消息通过sendTextMessage的返回值处理
                // 这里只处理表情等特殊消息
                val message = event.data as? String
                if (message != null && message.startsWith("表情:")) {
                    conversationViewModel.addMessage(
                        conversationId = conversationId ?: return,
                        role = MessageRole.ASSISTANT,
                        content = message
                    )
                }
            }
            XiaozhiServiceEventType.USER_MESSAGE -> {
                // 收到用户消息（语音转文本）
                val message = event.data as? String
                if (message != null) {
                    conversationViewModel.addMessage(
                        conversationId = conversationId ?: return,
                        role = MessageRole.USER,
                        content = message
                    )
                }
            }
            XiaozhiServiceEventType.ERROR -> {
                // 发生错误
                val error = event.data as? String
                runOnUiThread {
                    Toast.makeText(this, "小智服务错误: $error", Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                // 其他事件
            }
        }
    }
    
    private fun initializeXiaozhiService(configs: List<com.lhht.aiassistant.model.XiaozhiConfig>) {
        try {
            android.util.Log.d("ChatActivity", "初始化小智服务，配置数量: ${configs.size}")
            
            val xiaozhiConfig = if (configId != null && configId!!.isNotEmpty()) {
                android.util.Log.d("ChatActivity", "使用指定的configId: $configId")
                configs.find { it.id == configId }
            } else {
                android.util.Log.d("ChatActivity", "configId为空，获取第一个可用配置")
                configs.firstOrNull()
            }
            
            if (xiaozhiConfig != null) {
                android.util.Log.d("ChatActivity", "找到小智配置: ${xiaozhiConfig.name}")
                xiaozhiService = XiaozhiService.getInstance(
                    this@ChatActivity,
                    xiaozhiConfig.websocketUrl,
                    xiaozhiConfig.macAddress,
                    xiaozhiConfig.token
                )
                
                // 设置当前对话ID，确保消息路由到正确的对话
                xiaozhiService?.setCurrentConversationId(conversationId)
                
                // 添加事件监听器
                xiaozhiService?.addListener(::onXiaozhiServiceEvent)
                
                // 连接到服务
                scope.launch {
                    xiaozhiService?.connect()
                }
                android.util.Log.d("ChatActivity", "小智服务初始化成功")
            } else {
                android.util.Log.w("ChatActivity", "未找到小智配置")
                Toast.makeText(this@ChatActivity, "未找到小智配置，请先在设置中添加小智配置", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            android.util.Log.e("ChatActivity", "初始化小智服务失败", e)
            Toast.makeText(this@ChatActivity, "初始化小智服务失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // 清理资源
        scope.launch {
            xiaozhiService?.dispose()
        }
    }
}
