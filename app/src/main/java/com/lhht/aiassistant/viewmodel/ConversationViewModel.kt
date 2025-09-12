package com.lhht.aiassistant.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.lhht.aiassistant.database.AppDatabase
import com.lhht.aiassistant.model.*
import com.lhht.aiassistant.repository.ConversationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

/**
 * 对话ViewModel
 */
class ConversationViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getDatabase(application)
    private val repository = ConversationRepository(database)
    
    private val _conversations = MutableLiveData<List<Conversation>>()
    val conversations: LiveData<List<Conversation>> = _conversations
    
    private val _messages = MutableLiveData<Map<String, List<Message>>>()
    val messages: LiveData<Map<String, List<Message>>> = _messages
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    private val _currentConversationId = MutableLiveData<String?>()
    val currentConversationId: LiveData<String?> = _currentConversationId
    
    private val deletedConversations = mutableListOf<Conversation>()
    
    init {
        loadConversations()
    }
    
    /**
     * 加载对话列表
     */
    private fun loadConversations() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                // 持续观察数据库变化
                repository.getAllConversations().collect { conversations ->
                    _conversations.value = conversations
                    android.util.Log.d("ConversationViewModel", "对话列表更新: ${conversations.size} 个对话")
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                // 协程被取消是正常情况，不需要记录错误
                android.util.Log.d("ConversationViewModel", "对话列表加载被取消")
                throw e // 重新抛出CancellationException
            } catch (e: Exception) {
                _error.value = e.message
                android.util.Log.e("ConversationViewModel", "加载对话列表失败", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 添加对话
     */
    fun addConversation(conversation: Conversation) {
        viewModelScope.launch {
            try {
                repository.insertConversation(conversation)
                _currentConversationId.value = conversation.id
            } catch (e: kotlinx.coroutines.CancellationException) {
                android.util.Log.d("ConversationViewModel", "添加对话被取消")
                throw e
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    /**
     * 删除对话
     */
    fun deleteConversation(conversationId: String) {
        viewModelScope.launch {
            try {
                val conversation = repository.getConversationById(conversationId)
                if (conversation != null) {
                    deletedConversations.add(conversation)
                    repository.deleteConversation(conversationId)
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                android.util.Log.d("ConversationViewModel", "删除对话被取消")
                throw e
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    /**
     * 恢复最后删除的对话
     */
    fun restoreLastDeletedConversation() {
        viewModelScope.launch {
            try {
                if (deletedConversations.isNotEmpty()) {
                    val conversation = deletedConversations.removeAt(deletedConversations.size - 1)
                    repository.insertConversation(conversation)
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                android.util.Log.d("ConversationViewModel", "恢复对话被取消")
                throw e
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    /**
     * 切换置顶状态
     */
    fun togglePinConversation(conversationId: String) {
        viewModelScope.launch {
            try {
                val conversation = repository.getConversationById(conversationId)
                if (conversation != null) {
                    repository.togglePinConversation(conversationId, !conversation.isPinned)
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                android.util.Log.d("ConversationViewModel", "切换置顶状态被取消")
                throw e
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    /**
     * 标记对话为已读
     */
    fun markConversationAsRead(conversationId: String) {
        viewModelScope.launch {
            try {
                repository.markConversationAsRead(conversationId)
            } catch (e: kotlinx.coroutines.CancellationException) {
                android.util.Log.d("ConversationViewModel", "标记已读被取消")
                throw e
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    /**
     * 获取置顶对话
     */
    fun getPinnedConversations(): Flow<List<Conversation>> {
        return repository.getPinnedConversations()
    }
    
    /**
     * 获取非置顶对话
     */
    fun getUnpinnedConversations(): Flow<List<Conversation>> {
        return repository.getUnpinnedConversations()
    }
    
    /**
     * 添加消息
     */
    fun addMessage(
        conversationId: String,
        role: MessageRole,
        content: String,
        isImage: Boolean = false,
        imageLocalPath: String? = null,
        fileId: String? = null
    ) {
        viewModelScope.launch {
            try {
                val message = Message(
                    id = UUID.randomUUID().toString(),
                    conversationId = conversationId,
                    role = role,
                    content = content,
                    timestamp = Date(),
                    isImage = isImage,
                    imageLocalPath = imageLocalPath,
                    fileId = fileId
                )
                
                repository.insertMessage(message)
                android.util.Log.d("ConversationViewModel", "消息已保存: $content")
                
                // 更新对话的最后消息
                repository.updateConversationLastMessage(conversationId, content)
                android.util.Log.d("ConversationViewModel", "对话最后消息已更新: $conversationId -> $content")
            } catch (e: kotlinx.coroutines.CancellationException) {
                android.util.Log.d("ConversationViewModel", "添加消息被取消")
                throw e
            } catch (e: Exception) {
                _error.value = e.message
                android.util.Log.e("ConversationViewModel", "添加消息失败", e)
            }
        }
    }
    
    /**
     * 更新最后一条用户消息
     */
    fun updateLastUserMessage(
        conversationId: String,
        content: String,
        fileId: String? = null,
        isImage: Boolean = false,
        imageLocalPath: String? = null
    ) {
        viewModelScope.launch {
            try {
                repository.updateLastUserMessage(conversationId, content, fileId, isImage, imageLocalPath)
            } catch (e: kotlinx.coroutines.CancellationException) {
                android.util.Log.d("ConversationViewModel", "更新用户消息被取消")
                throw e
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    /**
     * 获取对话的消息列表
     */
    fun getMessages(conversationId: String): LiveData<List<Message>> {
        return repository.getMessagesByConversationId(conversationId).asLiveData()
    }
    
    /**
     * 获取对话的消息列表（同步）
     */
    suspend fun getMessagesSync(conversationId: String): List<Message> {
        return repository.getMessagesByConversationIdSync(conversationId)
    }
    
    /**
     * 设置当前对话ID
     */
    fun setCurrentConversationId(conversationId: String?) {
        _currentConversationId.value = conversationId
    }
    
    /**
     * 获取当前对话ID
     */
    fun getCurrentConversationId(): String? {
        return _currentConversationId.value
    }
    
    /**
     * 清除错误
     */
    fun clearError() {
        _error.value = null
    }
}
