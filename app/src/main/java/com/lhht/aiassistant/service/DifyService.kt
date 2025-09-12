package com.lhht.aiassistant.service

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.lhht.aiassistant.utils.DeviceUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Dify服务类
 */
class DifyService private constructor(
    private val context: Context,
    private val apiKey: String,
    private val apiUrl: String
) {
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
    
    private val gson = Gson()
    private val deviceUserId = DeviceUtil.getMacAddress(context)
    
    // 存储会话ID与conversation ID的映射表
    private val sessionConversationMap = mutableMapOf<String, String>()
    
    companion object {
        private const val CONVERSATION_MAP_KEY = "dify_conversation_map"
        
        suspend fun create(
            context: Context,
            apiKey: String,
            apiUrl: String
        ): DifyService {
            return withContext(Dispatchers.IO) {
                val service = DifyService(context, apiKey, apiUrl)
                service.loadConversationMap()
                service
            }
        }
    }
    
    /**
     * 从本地存储加载会话ID映射表
     */
    private fun loadConversationMap() {
        try {
            val prefs = context.getSharedPreferences("dify_service", Context.MODE_PRIVATE)
            val mapJson = prefs.getString(CONVERSATION_MAP_KEY, null)
            
            if (mapJson != null && mapJson.isNotEmpty()) {
                val loadedMap = gson.fromJson(mapJson, Map::class.java) as Map<String, String>
                sessionConversationMap.clear()
                sessionConversationMap.putAll(loadedMap)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 保存会话ID映射表到本地存储
     */
    private fun saveConversationMap() {
        try {
            val prefs = context.getSharedPreferences("dify_service", Context.MODE_PRIVATE)
            val mapJson = gson.toJson(sessionConversationMap)
            prefs.edit().putString(CONVERSATION_MAP_KEY, mapJson).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 确保URL使用HTTP/HTTPS协议
     */
    private fun ensureHttpProtocol(url: String): String {
        var sanitizedUrl = url
        
        // 替换协议
        when {
            sanitizedUrl.startsWith("ws://") -> {
                sanitizedUrl = sanitizedUrl.replaceFirst("ws://", "http://")
            }
            sanitizedUrl.startsWith("wss://") -> {
                sanitizedUrl = sanitizedUrl.replaceFirst("wss://", "https://")
            }
            !sanitizedUrl.startsWith("http://") && !sanitizedUrl.startsWith("https://") -> {
                sanitizedUrl = "https://$sanitizedUrl"
            }
        }
        
        // 去除URL末尾的斜杠
        while (sanitizedUrl.endsWith("/")) {
            sanitizedUrl = sanitizedUrl.substring(0, sanitizedUrl.length - 1)
        }
        
        return sanitizedUrl
    }
    
    /**
     * 获取会话的conversation_id
     */
    private fun getConversationId(sessionId: String): String? {
        return sessionConversationMap[sessionId]?.takeIf { it.isNotEmpty() }
    }
    
    /**
     * 发送消息
     */
    suspend fun sendMessage(
        message: String,
        sessionId: String = "default_session",
        forceNewConversation: Boolean = false,
        fileIds: List<String>? = null
    ): String = withContext(Dispatchers.IO) {
        
        // 如果强制创建新对话，清除当前会话的conversation_id
        if (forceNewConversation) {
            sessionConversationMap.remove(sessionId)
            saveConversationMap()
        }
        
        // 获取conversation_id
        val conversationId = getConversationId(sessionId)
        
        // 确保URL使用HTTP/HTTPS协议
        val sanitizedApiUrl = ensureHttpProtocol(apiUrl)
        val requestUrl = "$sanitizedApiUrl/chat-messages"
        
        // 构建请求体
        val requestBody = JsonObject().apply {
            add("inputs", JsonObject())
            addProperty("query", message)
            addProperty("response_mode", "blocking")
            addProperty("user", deviceUserId as String)
            
            if (conversationId != null) {
                addProperty("conversation_id", conversationId)
            }
            
            if (fileIds != null && fileIds.isNotEmpty()) {
                val filesArray = fileIds.map { fileId ->
                    JsonObject().apply {
                        addProperty("type", "image")
                        addProperty("transfer_method", "local_file")
                        addProperty("upload_file_id", fileId)
                    }
                }
                add("files", gson.toJsonTree(filesArray))
            }
        }
        
        val request = Request.Builder()
            .url(requestUrl)
            .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .build()
        
        try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            
            if (response.isSuccessful && responseBody != null) {
                val data = gson.fromJson(responseBody, JsonObject::class.java)
                
                // 保存服务器返回的conversation_id
                if (data.has("conversation_id")) {
                    val newConversationId = data.get("conversation_id").asString
                    sessionConversationMap[sessionId] = newConversationId
                    saveConversationMap()
                }
                
                return@withContext data.get("answer")?.asString ?: "无回复"
            } else {
                // 特殊处理404错误
                if (response.code == 404 && responseBody?.contains("Conversation Not Exists") == true) {
                    sessionConversationMap.remove(sessionId)
                    saveConversationMap()
                    // 递归调用自身
                    return@withContext sendMessage(message, sessionId, false, fileIds)
                }
                
                throw IOException("API请求失败: ${response.code}, 响应: $responseBody")
            }
        } catch (e: Exception) {
            throw Exception("发送消息失败: ${e.message}")
        }
    }
    
    /**
     * 上传文件
     */
    suspend fun uploadFile(file: File): Map<String, Any> = withContext(Dispatchers.IO) {
        val sanitizedApiUrl = ensureHttpProtocol(apiUrl)
        val requestUrl = "$sanitizedApiUrl/files/upload"
        
        if (!file.exists()) {
            throw Exception("文件不存在: ${file.path}")
        }
        
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name, file.asRequestBody("application/octet-stream".toMediaType()))
            .addFormDataPart("user", deviceUserId)
            .build()
        
        val request = Request.Builder()
            .url(requestUrl)
            .post(requestBody)
            .addHeader("Authorization", "Bearer $apiKey")
            .build()
        
        try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            
            if (response.isSuccessful && responseBody != null) {
                return@withContext gson.fromJson(responseBody, Map::class.java) as Map<String, Any>
            } else {
                throw IOException("文件上传失败: ${response.code}, 响应: $responseBody")
            }
        } catch (e: Exception) {
            throw Exception("上传文件失败: ${e.message}")
        }
    }
    
    /**
     * 清除特定会话的conversationId
     */
    fun clearConversation(sessionId: String) {
        sessionConversationMap.remove(sessionId)
        saveConversationMap()
    }
    
    /**
     * 清除所有会话
     */
    fun clearAllConversations() {
        sessionConversationMap.clear()
        saveConversationMap()
    }
}
