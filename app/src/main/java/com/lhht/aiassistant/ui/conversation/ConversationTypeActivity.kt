package com.lhht.aiassistant.ui.conversation

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.lhht.aiassistant.R
import com.lhht.aiassistant.databinding.ActivityConversationTypeBinding
import com.lhht.aiassistant.model.Conversation
import com.lhht.aiassistant.model.ConversationType
import com.lhht.aiassistant.ui.chat.ChatActivity
import com.lhht.aiassistant.ui.config.ConfigSelectionActivity
import com.lhht.aiassistant.viewmodel.ConversationViewModel
import java.util.*

/**
 * 对话类型选择Activity
 */
class ConversationTypeActivity : AppCompatActivity() {
    
    companion object {
        private const val REQUEST_CODE_SELECT_CONFIG = 1001
    }
    
    private lateinit var binding: ActivityConversationTypeBinding
    private lateinit var conversationViewModel: ConversationViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConversationTypeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupClickListeners()
        
        conversationViewModel = ViewModelProvider(this)[ConversationViewModel::class.java]
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.select_conversation_type)
    }
    
    private fun setupClickListeners() {
        binding.difyCard.setOnClickListener {
            android.util.Log.d("ConversationType", "点击Dify对话")
            createConversation(ConversationType.DIFY)
        }
        
        binding.xiaozhiCard.setOnClickListener {
            android.util.Log.d("ConversationType", "点击小智对话")
            createConversation(ConversationType.XIAOZHI)
        }
    }
    
    private fun createConversation(type: ConversationType) {
        android.util.Log.d("ConversationType", "创建对话，类型: $type")
        
        when (type) {
            ConversationType.DIFY -> {
                // Dify对话直接创建
                val conversation = Conversation(
                    id = UUID.randomUUID().toString(),
                    title = "Dify对话",
                    type = type,
                    lastMessageTime = Date(),
                    lastMessage = "开始新对话"
                )
                
                // 添加到ViewModel
                conversationViewModel.addConversation(conversation)
                
                // 打开聊天界面
                val intent = Intent(this, ChatActivity::class.java).apply {
                    putExtra("conversation_id", conversation.id)
                    putExtra("conversation_title", conversation.title)
                    putExtra("conversation_type", conversation.type.name)
                    putExtra("config_id", conversation.configId)
                }
                startActivity(intent)
                finish()
            }
            ConversationType.XIAOZHI -> {
                // 小智对话需要先选择配置
                val intent = Intent(this, ConfigSelectionActivity::class.java)
                startActivityForResult(intent, REQUEST_CODE_SELECT_CONFIG)
            }
        }
    }
    
    /**
     * 继续现有对话
     */
    fun continueConversation(conversation: Conversation) {
        android.util.Log.d("ConversationType", "继续对话: ${conversation.title}")
        
        // 打开聊天界面
        val intent = Intent(this, ChatActivity::class.java).apply {
            putExtra("conversation_id", conversation.id)
            putExtra("conversation_title", conversation.title)
            putExtra("conversation_type", conversation.type.name)
            putExtra("config_id", conversation.configId)
        }
        startActivity(intent)
        finish()
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == REQUEST_CODE_SELECT_CONFIG) {
            if (resultCode == RESULT_OK && data != null) {
                val configId = data.getStringExtra("selected_config_id")
                val configName = data.getStringExtra("selected_config_name")
                
                if (configId != null && configName != null) {
                    // 创建小智对话
                    val conversation = Conversation(
                        id = UUID.randomUUID().toString(),
                        title = configName,
                        type = ConversationType.XIAOZHI,
                        configId = configId,
                        lastMessageTime = Date(),
                        lastMessage = "开始新对话"
                    )
                    
                    // 添加到ViewModel
                    conversationViewModel.addConversation(conversation)
                    
                    // 打开聊天界面
                    val intent = Intent(this, ChatActivity::class.java).apply {
                        putExtra("conversation_id", conversation.id)
                        putExtra("conversation_title", conversation.title)
                        putExtra("conversation_type", conversation.type.name)
                        putExtra("config_id", conversation.configId)
                    }
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
