package com.lhht.aiassistant.model

import java.util.Date

/**
 * 消息模型
 */
data class Message(
    val id: String,
    val conversationId: String,
    val role: MessageRole,
    val content: String,
    val timestamp: Date,
    val isRead: Boolean = false,
    val isImage: Boolean = false,
    val imageLocalPath: String? = null,
    val fileId: String? = null
) {
    companion object {
        fun fromJson(json: Map<String, Any>): Message {
            return Message(
                id = json["id"] as String,
                conversationId = json["conversationId"] as String,
                role = MessageRole.valueOf((json["role"] as String).uppercase()),
                content = json["content"] as String,
                timestamp = Date((json["timestamp"] as String).toLong()),
                isRead = (json["isRead"] as? Boolean) ?: false,
                isImage = (json["isImage"] as? Boolean) ?: false,
                imageLocalPath = json["imageLocalPath"] as? String,
                fileId = json["fileId"] as? String
            )
        }
    }
    
    fun toJson(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "conversationId" to conversationId,
            "role" to role.name.lowercase(),
            "content" to content,
            "timestamp" to timestamp.time.toString(),
            "isRead" to isRead,
            "isImage" to isImage,
            "imageLocalPath" to (imageLocalPath ?: ""),
            "fileId" to (fileId ?: "")
        )
    }
}
