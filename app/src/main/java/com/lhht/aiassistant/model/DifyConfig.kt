package com.lhht.aiassistant.model

/**
 * Dify配置模型
 */
data class DifyConfig(
    val id: String,
    val name: String,
    val apiUrl: String,
    val apiKey: String
) {
    companion object {
        fun fromJson(json: Map<String, Any>): DifyConfig {
            return DifyConfig(
                id = json["id"] as String,
                name = json["name"] as String,
                apiUrl = json["apiUrl"] as String,
                apiKey = json["apiKey"] as String
            )
        }
    }
    
    fun toJson(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "name" to name,
            "apiUrl" to apiUrl,
            "apiKey" to apiKey
        )
    }
    
    fun copyWith(
        id: String? = null,
        name: String? = null,
        apiUrl: String? = null,
        apiKey: String? = null
    ): DifyConfig {
        return DifyConfig(
            id = id ?: this.id,
            name = name ?: this.name,
            apiUrl = apiUrl ?: this.apiUrl,
            apiKey = apiKey ?: this.apiKey
        )
    }
}
