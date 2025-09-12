package com.lhht.aiassistant.database.dao

import androidx.room.*
import com.lhht.aiassistant.database.entity.ConversationEntity
import kotlinx.coroutines.flow.Flow

/**
 * 对话DAO
 */
@Dao
interface ConversationDao {
    
    @Query("SELECT * FROM conversations ORDER BY isPinned DESC, lastMessageTime DESC")
    fun getAllConversations(): Flow<List<ConversationEntity>>
    
    @Query("SELECT * FROM conversations WHERE id = :conversationId")
    suspend fun getConversationById(conversationId: String): ConversationEntity?
    
    @Query("SELECT * FROM conversations WHERE isPinned = 1 ORDER BY lastMessageTime DESC")
    fun getPinnedConversations(): Flow<List<ConversationEntity>>
    
    @Query("SELECT * FROM conversations WHERE isPinned = 0 ORDER BY lastMessageTime DESC")
    fun getUnpinnedConversations(): Flow<List<ConversationEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversations(conversations: List<ConversationEntity>)
    
    @Update
    suspend fun updateConversation(conversation: ConversationEntity)
    
    @Delete
    suspend fun deleteConversation(conversation: ConversationEntity)
    
    @Query("DELETE FROM conversations WHERE id = :conversationId")
    suspend fun deleteConversationById(conversationId: String)
    
    @Query("UPDATE conversations SET isPinned = :isPinned, updatedAt = :updatedAt WHERE id = :conversationId")
    suspend fun updatePinStatus(conversationId: String, isPinned: Boolean, updatedAt: Long)
    
    @Query("UPDATE conversations SET unreadCount = 0, updatedAt = :updatedAt WHERE id = :conversationId")
    suspend fun markAsRead(conversationId: String, updatedAt: Long)
    
    @Query("UPDATE conversations SET lastMessage = :lastMessage, lastMessageTime = :lastMessageTime, updatedAt = :updatedAt WHERE id = :conversationId")
    suspend fun updateLastMessage(conversationId: String, lastMessage: String, lastMessageTime: Long, updatedAt: Long)
    
    @Query("SELECT COUNT(*) FROM conversations")
    suspend fun getConversationCount(): Int
    
    @Query("DELETE FROM conversations")
    suspend fun deleteAllConversations()
}
