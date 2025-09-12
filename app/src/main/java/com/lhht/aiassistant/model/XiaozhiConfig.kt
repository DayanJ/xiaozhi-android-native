package com.lhht.aiassistant.model

/**
 * 小智配置模型
 */
data class XiaozhiConfig(
    val id: String,
    val name: String,
    val websocketUrl: String,
    val macAddress: String,
    val token: String
) {
    companion object {
        fun fromJson(json: Map<String, Any>): XiaozhiConfig {
            return XiaozhiConfig(
                id = json["id"] as String,
                name = json["name"] as String,
                websocketUrl = json["websocketUrl"] as String,
                macAddress = json["macAddress"] as String,
                token = json["token"] as String
            )
        }
    }
    
    fun toJson(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "name" to name,
            "websocketUrl" to websocketUrl,
            "macAddress" to macAddress,
            "token" to token
        )
    }
    
    fun copyWith(
        name: String? = null,
        websocketUrl: String? = null,
        macAddress: String? = null,
        token: String? = null
    ): XiaozhiConfig {
        return XiaozhiConfig(
            id = id,
            name = name ?: this.name,
            websocketUrl = websocketUrl ?: this.websocketUrl,
            macAddress = macAddress ?: this.macAddress,
            token = token ?: this.token
        )
    }
}
