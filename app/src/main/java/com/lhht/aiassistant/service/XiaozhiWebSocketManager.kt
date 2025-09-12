package com.lhht.aiassistant.service

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.WebSocket
import java.util.concurrent.TimeUnit

/**
 * 会话ID更新回调
 */
interface SessionIdCallback {
    fun onSessionIdUpdated(sessionId: String)
}

/**
 * 小智WebSocket管理器
 */
class XiaozhiWebSocketManager(
    private val deviceId: String,
    private val enableToken: Boolean = false
) {
    
    companion object {
        private const val TAG = "XiaozhiWebSocketManager"
    }
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.SECONDS) // 无限读取超时
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val gson = Gson()
    private var webSocket: WebSocket? = null
    private var isConnected = false
    private val listeners = mutableListOf<XiaozhiServiceListener>()
    private var sessionIdCallback: SessionIdCallback? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // 长连接管理
    private var reconnectJob: Job? = null
    private var heartbeatJob: Job? = null
    private var shouldReconnect = true
    private var reconnectDelay = 2000L // 2秒重连延迟
    private var maxReconnectDelay = 10000L // 最大10秒重连延迟
    private var currentReconnectDelay = reconnectDelay
    
    // 连接参数存储
    private var storedWebsocketUrl: String? = null
    private var storedToken: String? = null
    
    /**
     * 添加事件监听器
     */
    fun addListener(listener: XiaozhiServiceListener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
        }
    }
    
    /**
     * 移除事件监听器
     */
    fun removeListener(listener: XiaozhiServiceListener) {
        listeners.remove(listener)
    }
    
    /**
     * 移除所有事件监听器
     */
    fun removeAllListeners() {
        listeners.clear()
    }
    
    /**
     * 设置会话ID回调
     */
    fun setSessionIdCallback(callback: SessionIdCallback?) {
        sessionIdCallback = callback
    }
    
    /**
     * 分发事件到所有监听器
     */
    private fun dispatchEvent(event: XiaozhiServiceEvent) {
        Log.d(TAG, "Dispatching event: ${event.type}, listeners count: ${listeners.size}")
        listeners.forEach { listener ->
            try {
                listener(event)
            } catch (e: Exception) {
                Log.e(TAG, "Error in event listener", e)
            }
        }
    }
    
    /**
     * 连接到WebSocket服务器
     * 参考Flutter工程中的连接协议，支持长连接管理
     */
    suspend fun connect(websocketUrl: String, token: String? = null) = withContext(Dispatchers.IO) {
        if (isConnected && webSocket != null) {
            Log.d(TAG, "Already connected, skipping connection")
            return@withContext
        }
        
        // 如果之前有连接但已断开，先清理
        if (webSocket != null && !isConnected) {
            Log.d(TAG, "Cleaning up previous connection")
            try {
                webSocket?.close(1000, "Reconnecting")
            } catch (e: Exception) {
                Log.w(TAG, "Error closing previous connection", e)
            }
            webSocket = null
        }
        
        // 存储连接参数用于重连
        storedWebsocketUrl = websocketUrl
        storedToken = token
        
        try {
            Log.d(TAG, "Connecting to WebSocket: $websocketUrl")
            Log.d(TAG, "Device ID: $deviceId")
            Log.d(TAG, "Token enabled: $enableToken")
            
            // 构建请求，添加必要的headers
            val requestBuilder = Request.Builder()
                .url(websocketUrl)
                .addHeader("device-id", deviceId)
                .addHeader("client-id", deviceId)
                .addHeader("protocol-version", "1")
            
            // 添加Authorization头
            if (enableToken && token != null && token.isNotEmpty()) {
                requestBuilder.addHeader("Authorization", "Bearer $token")
                Log.d(TAG, "Added Authorization header: Bearer $token")
            } else {
                requestBuilder.addHeader("Authorization", "Bearer test-token")
                Log.d(TAG, "Added default Authorization header: Bearer test-token")
            }
            
            val request = requestBuilder.build()
            
            webSocket = client.newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    Log.d(TAG, "WebSocket connected")
                    isConnected = true
                    
                    // 重置重连延迟
                    currentReconnectDelay = reconnectDelay
                    
                    // 取消重连任务
                    reconnectJob?.cancel()
                    reconnectJob = null
                    
                    // 启动心跳
                    startHeartbeat()
                    
                    dispatchEvent(XiaozhiServiceEvent(XiaozhiServiceEventType.CONNECTED, null))
                    
                    // 发送Hello消息
                    sendHelloMessage()
                }
                
                override fun onMessage(webSocket: WebSocket, text: String) {
                    Log.d(TAG, "Received text message: $text")
                    handleTextMessage(text)
                }
                
                override fun onMessage(webSocket: WebSocket, bytes: okio.ByteString) {
                    Log.d(TAG, "Received binary message: ${bytes.size} bytes")
                    Log.d(TAG, "Dispatching AUDIO_DATA event for ${bytes.size} bytes")
                    dispatchEvent(XiaozhiServiceEvent(XiaozhiServiceEventType.AUDIO_DATA, bytes.toByteArray()))
                }
                
                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    Log.d(TAG, "WebSocket closing: $code $reason")
                    handleDisconnection()
                }
                
                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    Log.d(TAG, "WebSocket closed: $code $reason")
                    handleDisconnection()
                }
                
                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    Log.e(TAG, "WebSocket failure", t)
                    handleDisconnection()
                }
            })
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to connect to WebSocket", e)
            dispatchEvent(XiaozhiServiceEvent(XiaozhiServiceEventType.ERROR, e.message))
            throw e
        }
    }
    
    /**
     * 发送Hello消息
     * 参考Flutter工程中的_sendHelloMessage方法
     */
    private fun sendHelloMessage() {
        val helloMessage = JsonObject().apply {
            addProperty("type", "hello")
            addProperty("version", 1)
            addProperty("transport", "websocket")
            
            // 添加音频参数
            val audioParams = JsonObject().apply {
                addProperty("format", "opus")
                addProperty("sample_rate", 16000)
                addProperty("channels", 1)
                addProperty("frame_duration", 60)
            }
            add("audio_params", audioParams)
        }
        
        Log.d(TAG, "Sending hello message: ${gson.toJson(helloMessage)}")
        sendMessage(gson.toJson(helloMessage))
    }
    
    /**
     * 处理文本消息
     * 参考Flutter工程中的消息处理逻辑
     */
    private fun handleTextMessage(message: String) {
        try {
            Log.d(TAG, "Processing message: $message")
            val jsonData = gson.fromJson(message, JsonObject::class.java)
            val type = jsonData.get("type")?.asString ?: ""
            
            when (type) {
                "hello" -> {
                    Log.d(TAG, "Received hello response")
                    // 更新会话ID
                    val sessionId = jsonData.get("session_id")?.asString
                    if (sessionId != null) {
                        Log.d(TAG, "Updated session ID: $sessionId")
                        sessionIdCallback?.onSessionIdUpdated(sessionId)
                    }
                }
                "tts" -> {
                    val state = jsonData.get("state")?.asString ?: ""
                    val text = jsonData.get("text")?.asString ?: ""
                    
                    Log.d(TAG, "Received TTS message - state: $state, text: $text")
                    
                    when (state) {
                        "start" -> {
                            Log.d(TAG, "TTS started")
                            dispatchEvent(XiaozhiServiceEvent(XiaozhiServiceEventType.TTS_STARTED, null))
                        }
                        "sentence_start" -> {
                            if (text.isNotEmpty()) {
                                Log.d(TAG, "Dispatching TEXT_MESSAGE for sentence_start: $text")
                                dispatchEvent(XiaozhiServiceEvent(XiaozhiServiceEventType.TEXT_MESSAGE, text))
                            } else {
                                Log.w(TAG, "TTS sentence_start but text is empty")
                            }
                        }
                        "sentence_end" -> {
                            Log.d(TAG, "TTS sentence ended")
                        }
                        "audio_start" -> {
                            Log.d(TAG, "TTS audio started")
                        }
                        "audio_end" -> {
                            Log.d(TAG, "TTS audio ended")
                        }
                        "stop" -> {
                            Log.d(TAG, "TTS stopped")
                            dispatchEvent(XiaozhiServiceEvent(XiaozhiServiceEventType.TTS_STOPPED, null))
                        }
                    }
                }
                "stt" -> {
                    val state = jsonData.get("state")?.asString ?: ""
                    val text = jsonData.get("text")?.asString ?: ""
                    
                    Log.d(TAG, "Received STT message - state: $state, text: $text")
                    
                    // 如果没有state字段但有text字段，直接处理为STT结果
                    if (state.isEmpty() && text.isNotEmpty()) {
                        Log.d(TAG, "STT result (no state): $text")
                        dispatchEvent(XiaozhiServiceEvent(XiaozhiServiceEventType.STT_RESULT, text))
                    } else {
                        // 有state字段的情况，按原有逻辑处理
                        when (state) {
                            "start" -> {
                                Log.d(TAG, "STT started")
                                dispatchEvent(XiaozhiServiceEvent(XiaozhiServiceEventType.STT_STARTED, null))
                            }
                            "partial" -> {
                                // 部分识别结果，可以用于实时显示
                                if (text.isNotEmpty()) {
                                    Log.d(TAG, "STT partial result: $text")
                                    dispatchEvent(XiaozhiServiceEvent(XiaozhiServiceEventType.STT_RESULT, text))
                                }
                            }
                            "final" -> {
                                // 最终识别结果
                                if (text.isNotEmpty()) {
                                    Log.d(TAG, "STT final result: $text")
                                    dispatchEvent(XiaozhiServiceEvent(XiaozhiServiceEventType.STT_RESULT, text))
                                }
                            }
                            "stop" -> {
                                Log.d(TAG, "STT stopped")
                                dispatchEvent(XiaozhiServiceEvent(XiaozhiServiceEventType.STT_STOPPED, null))
                            }
                        }
                    }
                }
                "emotion" -> {
                    val emotion = jsonData.get("emotion")?.asString ?: ""
                    if (emotion.isNotEmpty()) {
                        Log.d(TAG, "Received emotion: $emotion")
                        dispatchEvent(XiaozhiServiceEvent(XiaozhiServiceEventType.TEXT_MESSAGE, "表情: $emotion"))
                    }
                }
                "listen" -> {
                    val state = jsonData.get("state")?.asString ?: ""
                    Log.d(TAG, "Received listen message - state: $state")
                    
                    when (state) {
                        "start" -> {
                            Log.d(TAG, "Listening started")
                        }
                        "end" -> {
                            Log.d(TAG, "Listening ended")
                        }
                        "detect" -> {
                            Log.d(TAG, "Voice detected")
                        }
                    }
                }
                "error" -> {
                    val error = jsonData.get("error")?.asString ?: "Unknown error"
                    Log.e(TAG, "Received error message: $error")
                    dispatchEvent(XiaozhiServiceEvent(XiaozhiServiceEventType.ERROR, error))
                }
                else -> {
                    Log.d(TAG, "Received unknown message type: $type")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse message: $message", e)
        }
    }
    
    /**
     * 发送文本消息
     */
    fun sendMessage(message: String) {
        webSocket?.send(message)
    }
    
    /**
     * 发送二进制消息
     */
    fun sendBinaryMessage(data: ByteArray) {
        webSocket?.send(okio.ByteString.of(*data))
    }
    
    /**
     * 发送文本请求
     * 参考Flutter工程中的sendTextRequest方法
     */
    fun sendTextRequest(text: String) {
        if (!isConnected) {
            Log.w(TAG, "Cannot send text request, not connected")
            return
        }

        try {
            val request = JsonObject().apply {
                addProperty("type", "listen")
                addProperty("state", "detect")
                addProperty("text", text)
                addProperty("source", "text")
            }
            
            Log.d(TAG, "Sending text request: ${gson.toJson(request)}")
            sendMessage(gson.toJson(request))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send text request", e)
        }
    }
    
    /**
     * 开始语音监听
     * 参考Flutter项目中的实现，支持会话ID和模式参数
     */
    fun startListening(sessionId: String? = null, mode: String = "auto") {
        if (!isConnected) {
            Log.w(TAG, "Cannot start listening, not connected")
            return
        }

        try {
            val request = JsonObject().apply {
                addProperty("type", "listen")
                addProperty("state", "start")
                addProperty("mode", mode)
                sessionId?.let { addProperty("session_id", it) }
            }
            
            Log.d(TAG, "Starting listening: ${gson.toJson(request)}")
            sendMessage(gson.toJson(request))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start listening", e)
        }
    }
    
    /**
     * 停止语音监听
     * 参考Flutter项目中的实现，支持会话ID和模式参数
     */
    fun stopListening(sessionId: String? = null, mode: String = "auto") {
        if (!isConnected) {
            Log.w(TAG, "Cannot stop listening, not connected")
            return
        }

        try {
            val request = JsonObject().apply {
                addProperty("type", "listen")
                addProperty("state", "stop")
                addProperty("mode", mode)
                sessionId?.let { addProperty("session_id", it) }
            }
            
            Log.d(TAG, "Stopping listening: ${gson.toJson(request)}")
            sendMessage(gson.toJson(request))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop listening", e)
        }
    }
    
    /**
     * 断开连接
     */
    suspend fun disconnect() = withContext(Dispatchers.IO) {
        try {
            webSocket?.close(1000, "Normal closure")
            webSocket = null
            isConnected = false
        } catch (e: Exception) {
            Log.e(TAG, "Error disconnecting WebSocket", e)
        }
    }
    
    /**
     * 检查是否已连接
     */
    fun isConnected(): Boolean = isConnected
    
    /**
     * 处理断开连接
     */
    private fun handleDisconnection() {
        isConnected = false
        
        // 停止心跳
        heartbeatJob?.cancel()
        heartbeatJob = null
        
        dispatchEvent(XiaozhiServiceEvent(XiaozhiServiceEventType.DISCONNECTED, null))
        
        // 启动自动重连
        if (shouldReconnect && storedWebsocketUrl != null) {
            startReconnect()
        }
    }
    
    /**
     * 启动自动重连
     */
    private fun startReconnect() {
        if (reconnectJob?.isActive == true) {
            return // 已经在重连中
        }
        
        reconnectJob = scope.launch {
            try {
                Log.d(TAG, "Starting auto-reconnect in ${currentReconnectDelay}ms")
                delay(currentReconnectDelay)
                
                if (shouldReconnect && !isConnected && storedWebsocketUrl != null) {
                    Log.d(TAG, "Attempting to reconnect...")
                    connect(storedWebsocketUrl!!, storedToken)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Reconnect failed", e)
                // 增加重连延迟，但不超过最大值
                currentReconnectDelay = minOf(currentReconnectDelay * 2, maxReconnectDelay)
                // 继续重连
                if (shouldReconnect) {
                    startReconnect()
                }
            }
        }
    }
    
    /**
     * 启动心跳
     */
    private fun startHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = scope.launch {
            try {
                while (isConnected) {
                    delay(15000) // 15秒心跳间隔，更频繁的心跳
                    if (isConnected) {
                        sendPing()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Heartbeat failed", e)
            }
        }
    }
    
    /**
     * 发送心跳ping
     */
    private fun sendPing() {
        try {
            if (isConnected && webSocket != null) {
                val pingMessage = JsonObject().apply {
                    addProperty("type", "ping")
                    addProperty("timestamp", System.currentTimeMillis())
                }
                sendMessage(gson.toJson(pingMessage))
                Log.d(TAG, "Sent heartbeat ping")
            } else {
                Log.w(TAG, "Cannot send ping: isConnected=$isConnected, webSocket=${webSocket != null}")
                // 如果连接状态不一致，触发重连
                if (webSocket != null && !isConnected) {
                    Log.w(TAG, "Connection state inconsistent, triggering reconnection")
                    handleDisconnection()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send ping", e)
            // 发送ping失败，可能连接已断开
            handleDisconnection()
        }
    }
    
    /**
     * 设置是否启用自动重连
     */
    fun setAutoReconnect(enabled: Boolean) {
        shouldReconnect = enabled
        if (!enabled) {
            reconnectJob?.cancel()
            reconnectJob = null
        }
    }
    
    /**
     * 释放资源
     */
    fun dispose() {
        shouldReconnect = false
        reconnectJob?.cancel()
        heartbeatJob?.cancel()
        scope.cancel()
        webSocket?.close(1000, "Disposing")
        webSocket = null
        isConnected = false
        listeners.clear()
    }
}
