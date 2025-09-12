package com.lhht.aiassistant.repository

import com.lhht.aiassistant.database.AppDatabase
import com.lhht.aiassistant.database.entity.ConversationEntity
import com.lhht.aiassistant.database.entity.MessageEntity
import com.lhht.aiassistant.model.Conversation
import com.lhht.aiassistant.model.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 对话仓库
 */
class ConversationRepository(private val database: AppDatabase) {
    
    private val conversationDao = database.conversationDao()
    private val messageDao = database.messageDao()
    
    // 对话相关操作
    fun getAllConversations(): Flow<List<Conversation>> {
        return conversationDao.getAllConversations().map { entities ->
            entities.map { it.toConversation() }
        }
    }
    
    suspend fun getConversationById(conversationId: String): Conversation? {
        return conversationDao.getConversationById(conversationId)?.toConversation()
    }
    
    fun getPinnedConversations(): Flow<List<Conversation>> {
        return conversationDao.getPinnedConversations().map { entities ->
            entities.map { it.toConversation() }
        }
    }
    
    fun getUnpinnedConversations(): Flow<List<Conversation>> {
        return conversationDao.getUnpinnedConversations().map { entities ->
            entities.map { it.toConversation() }
        }
    }
    
    suspend fun insertConversation(conversation: Conversation) {
        conversationDao.insertConversation(ConversationEntity.fromConversation(conversation))
    }
    
    suspend fun updateConversation(conversation: Conversation) {
        conversationDao.updateConversation(ConversationEntity.fromConversation(conversation))
    }
    
    suspend fun deleteConversation(conversationId: String) {
        conversationDao.deleteConversationById(conversationId)
        // 同时删除该对话的所有消息
        messageDao.deleteMessagesByConversationId(conversationId)
    }
    
    suspend fun togglePinConversation(conversationId: String, isPinned: Boolean) {
        conversationDao.updatePinStatus(conversationId, isPinned, System.currentTimeMillis())
    }
    
    suspend fun markConversationAsRead(conversationId: String) {
        conversationDao.markAsRead(conversationId, System.currentTimeMillis())
        messageDao.markMessagesAsRead(conversationId)
    }
    
    suspend fun updateConversationLastMessage(conversationId: String, lastMessage: String) {
        conversationDao.updateLastMessage(
            conversationId, 
            lastMessage, 
            System.currentTimeMillis(), 
            System.currentTimeMillis()
        )
    }
    
    // 消息相关操作
    fun getMessagesByConversationId(conversationId: String): Flow<List<Message>> {
        return messageDao.getMessagesByConversationId(conversationId).map { entities ->
            entities.map { it.toMessage() }
        }
    }
    
    suspend fun getMessagesByConversationIdSync(conversationId: String): List<Message> {
        return messageDao.getMessagesByConversationIdSync(conversationId).map { it.toMessage() }
    }
    
    suspend fun insertMessage(message: Message) {
        messageDao.insertMessage(MessageEntity.fromMessage(message))
    }
    
    suspend fun updateMessage(message: Message) {
        messageDao.updateMessage(MessageEntity.fromMessage(message))
    }
    
    suspend fun deleteMessage(messageId: String) {
        messageDao.deleteMessageById(messageId)
    }
    
    suspend fun getUnreadMessageCount(conversationId: String): Int {
        return messageDao.getUnreadMessageCount(conversationId)
    }
    
    suspend fun getLastUserMessage(conversationId: String): Message? {
        return messageDao.getLastMessageByRole(conversationId, "user")?.toMessage()
    }
    
    suspend fun updateLastUserMessage(conversationId: String, content: String, fileId: String? = null, isImage: Boolean = false, imageLocalPath: String? = null) {
        val lastUserMessage = messageDao.getLastMessageByRole(conversationId, "user")
        if (lastUserMessage != null) {
            val updatedMessage = lastUserMessage.copy(
                content = content,
                fileId = fileId,
                isImage = isImage,
                imageLocalPath = imageLocalPath
            )
            messageDao.updateMessage(updatedMessage)
        }
    }
}
