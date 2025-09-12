package com.lhht.aiassistant.database.dao

import androidx.room.*
import com.lhht.aiassistant.database.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

/**
 * 消息DAO
 */
@Dao
interface MessageDao {
    
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesByConversationId(conversationId: String): Flow<List<MessageEntity>>
    
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    suspend fun getMessagesByConversationIdSync(conversationId: String): List<MessageEntity>
    
    @Query("SELECT * FROM messages WHERE id = :messageId")
    suspend fun getMessageById(messageId: String): MessageEntity?
    
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId AND role = :role ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastMessageByRole(conversationId: String, role: String): MessageEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)
    
    @Update
    suspend fun updateMessage(message: MessageEntity)
    
    @Delete
    suspend fun deleteMessage(message: MessageEntity)
    
    @Query("DELETE FROM messages WHERE id = :messageId")
    suspend fun deleteMessageById(messageId: String)
    
    @Query("DELETE FROM messages WHERE conversationId = :conversationId")
    suspend fun deleteMessagesByConversationId(conversationId: String)
    
    @Query("UPDATE messages SET isRead = 1 WHERE conversationId = :conversationId")
    suspend fun markMessagesAsRead(conversationId: String)
    
    @Query("SELECT COUNT(*) FROM messages WHERE conversationId = :conversationId")
    suspend fun getMessageCount(conversationId: String): Int
    
    @Query("SELECT COUNT(*) FROM messages WHERE conversationId = :conversationId AND isRead = 0")
    suspend fun getUnreadMessageCount(conversationId: String): Int
    
    @Query("DELETE FROM messages")
    suspend fun deleteAllMessages()
}
