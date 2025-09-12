package com.lhht.aiassistant

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.lhht.aiassistant.databinding.ActivityMainBinding
import com.lhht.aiassistant.model.Conversation
import com.lhht.aiassistant.ui.chat.ChatActivity
import com.lhht.aiassistant.ui.conversation.ConversationTypeActivity
import com.lhht.aiassistant.ui.main.ConversationAdapter
import com.lhht.aiassistant.ui.settings.SettingsActivity
import com.lhht.aiassistant.viewmodel.ConversationViewModel

/**
 * 主Activity
 */
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private val viewModel: ConversationViewModel by viewModels()
    private lateinit var conversationAdapter: ConversationAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupRecyclerView()
        setupFloatingActionButton()
        observeViewModel()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.messages)
    }
    
    private fun setupRecyclerView() {
        conversationAdapter = ConversationAdapter(
            onConversationClick = { conversation ->
                openChatActivity(conversation)
            },
            onConversationDelete = { conversation ->
                deleteConversation(conversation)
            }
        )
        
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = conversationAdapter
        }
    }
    
    private fun setupFloatingActionButton() {
        binding.fab.setOnClickListener {
            startActivity(Intent(this, ConversationTypeActivity::class.java))
        }
    }
    
    private fun observeViewModel() {
        viewModel.conversations.observe(this, Observer { conversations ->
            android.util.Log.d("MainActivity", "收到对话列表更新: ${conversations.size} 个对话")
            conversationAdapter.submitList(conversations)
            
            if (conversations.isEmpty()) {
                binding.emptyState.visibility = android.view.View.VISIBLE
                binding.recyclerView.visibility = android.view.View.GONE
            } else {
                binding.emptyState.visibility = android.view.View.GONE
                binding.recyclerView.visibility = android.view.View.VISIBLE
            }
        })
        
        viewModel.error.observe(this, Observer { error ->
            error?.let {
                // 显示错误消息
                android.widget.Toast.makeText(this, it, android.widget.Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        })
    }
    
    private fun openChatActivity(conversation: Conversation) {
        // 标记对话为已读
        viewModel.markConversationAsRead(conversation.id)
        
        val intent = Intent(this, ChatActivity::class.java).apply {
            putExtra("conversation_id", conversation.id)
            putExtra("conversation_title", conversation.title)
            putExtra("conversation_type", conversation.type.name)
            putExtra("config_id", conversation.configId)
        }
        startActivity(intent)
    }
    
    private fun deleteConversation(conversation: Conversation) {
        viewModel.deleteConversation(conversation.id)
        android.widget.Toast.makeText(this, "对话已删除", android.widget.Toast.LENGTH_SHORT).show()
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    override fun onResume() {
        super.onResume()
        // 当用户从聊天界面返回时，确保对话列表是最新的
        // 由于ViewModel已经持续观察数据库变化，这里不需要额外操作
        // 但可以添加一些调试日志来确认数据更新
        android.util.Log.d("MainActivity", "onResume: 对话列表应该自动更新")
    }
}
