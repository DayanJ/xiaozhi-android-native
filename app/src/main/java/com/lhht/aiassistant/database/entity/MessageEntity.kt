package com.lhht.aiassistant.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lhht.aiassistant.model.Message
import com.lhht.aiassistant.model.MessageRole

/**
 * 消息实体类
 */
@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey
    val id: String,
    val conversationId: String,
    val role: String, // MessageRole的字符串表示
    val content: String,
    val timestamp: Long, // 时间戳
    val isRead: Boolean = false,
    val isImage: Boolean = false,
    val imageLocalPath: String? = null,
    val fileId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toMessage(): Message {
        return Message(
            id = id,
            conversationId = conversationId,
            role = MessageRole.valueOf(role.uppercase()),
            content = content,
            timestamp = java.util.Date(timestamp),
            isRead = isRead,
            isImage = isImage,
            imageLocalPath = imageLocalPath,
            fileId = fileId
        )
    }
    
    companion object {
        fun fromMessage(message: Message): MessageEntity {
            return MessageEntity(
                id = message.id,
                conversationId = message.conversationId,
                role = message.role.name.lowercase(),
                content = message.content,
                timestamp = message.timestamp.time,
                isRead = message.isRead,
                isImage = message.isImage,
                imageLocalPath = message.imageLocalPath,
                fileId = message.fileId
            )
        }
    }
}
