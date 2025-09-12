package com.lhht.aiassistant.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lhht.aiassistant.model.Conversation
import com.lhht.aiassistant.model.ConversationType
import java.util.Date

/**
 * 对话实体类
 */
@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val type: String, // ConversationType的字符串表示
    val configId: String = "",
    val lastMessageTime: Long, // 时间戳
    val lastMessage: String,
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toConversation(): Conversation {
        return Conversation(
            id = id,
            title = title,
            type = ConversationType.valueOf(type.uppercase()),
            configId = configId,
            lastMessageTime = Date(lastMessageTime),
            lastMessage = lastMessage,
            unreadCount = unreadCount,
            isPinned = isPinned
        )
    }
    
    companion object {
        fun fromConversation(conversation: Conversation): ConversationEntity {
            return ConversationEntity(
                id = conversation.id,
                title = conversation.title,
                type = conversation.type.name.lowercase(),
                configId = conversation.configId,
                lastMessageTime = conversation.lastMessageTime.time,
                lastMessage = conversation.lastMessage,
                unreadCount = conversation.unreadCount,
                isPinned = conversation.isPinned
            )
        }
    }
}
