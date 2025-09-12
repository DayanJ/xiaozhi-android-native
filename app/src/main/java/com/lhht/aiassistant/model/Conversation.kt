package com.lhht.aiassistant.model

import java.util.Date

/**
 * 对话模型
 */
data class Conversation(
    val id: String,
    val title: String,
    val type: ConversationType,
    val configId: String = "",
    val lastMessageTime: Date,
    val lastMessage: String,
    val unreadCount: Int = 0,
    val isPinned: Boolean = false
) {
    companion object {
        fun fromJson(json: Map<String, Any>): Conversation {
            return Conversation(
                id = json["id"] as String,
                title = json["title"] as String,
                type = ConversationType.valueOf((json["type"] as String).uppercase()),
                configId = json["configId"] as? String ?: "",
                lastMessageTime = Date((json["lastMessageTime"] as String).toLong()),
                lastMessage = json["lastMessage"] as String,
                unreadCount = (json["unreadCount"] as? Number)?.toInt() ?: 0,
                isPinned = (json["isPinned"] as? Boolean) ?: false
            )
        }
    }
    
    fun toJson(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "title" to title,
            "type" to type.name.lowercase(),
            "configId" to configId,
            "lastMessageTime" to lastMessageTime.time.toString(),
            "lastMessage" to lastMessage,
            "unreadCount" to unreadCount,
            "isPinned" to isPinned
        )
    }
    
    fun copyWith(
        title: String? = null,
        type: ConversationType? = null,
        configId: String? = null,
        lastMessageTime: Date? = null,
        lastMessage: String? = null,
        unreadCount: Int? = null,
        isPinned: Boolean? = null
    ): Conversation {
        return Conversation(
            id = id,
            title = title ?: this.title,
            type = type ?: this.type,
            configId = configId ?: this.configId,
            lastMessageTime = lastMessageTime ?: this.lastMessageTime,
            lastMessage = lastMessage ?: this.lastMessage,
            unreadCount = unreadCount ?: this.unreadCount,
            isPinned = isPinned ?: this.isPinned
        )
    }
}
