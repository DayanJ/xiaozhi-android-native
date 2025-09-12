package com.lhht.aiassistant.service

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

/**
 * 小智服务类
 */
class XiaozhiService private constructor(
    private val context: Context,
    private val websocketUrl: String,
    private val macAddress: String,
    private val token: String
) : SessionIdCallback {
    
    companion object {
        private const val TAG = "XiaozhiService"
        
        @Volatile
        private var INSTANCE: XiaozhiService? = null
        
        fun getInstance(
            context: Context,
            websocketUrl: String,
            macAddress: String,
            token: String
        ): XiaozhiService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: XiaozhiService(
                    context.applicationContext,
                    websocketUrl,
                    macAddress,
                    token
                ).also { INSTANCE = it }
            }
        }
        
        /**
         * 重置单例实例（用于测试或重新初始化）
         */
        fun resetInstance() {
            synchronized(this) {
                INSTANCE?.let { instance ->
                    instance.webSocketManager?.removeAllListeners()
                    instance.listeners.clear()
                }
                INSTANCE = null
            }
        }
    }
    
    private val gson = Gson()
    private var webSocketManager: XiaozhiWebSocketManager? = null
    var audioUtil: AudioUtil? = null
        private set
    private var isConnected = false
    private var isVoiceCallActive = false
    private var isMuted = false
    private var sessionId: String? = null
    private var currentConversationId: String? = null // 当前对话ID
    
    private val listeners = mutableListOf<XiaozhiServiceListener>()
    private var messageListener: MessageListener? = null
    
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // 语音流相关
    private var audioStreamJob: Job? = null
    private var isVoiceStreaming = false
    
    // 语音识别结果回调
    private var voiceRecognitionCallback: ((String) -> Unit)? = null
    
    init {
        initialize()
    }
    
    /**
     * 初始化
     */
    private fun initialize() {
        audioUtil = AudioUtil.getInstance(context)
        webSocketManager = XiaozhiWebSocketManager(macAddress, true)
        webSocketManager?.addListener(::onWebSocketEvent)
        webSocketManager?.setSessionIdCallback(this)
        
        // 预初始化音频组件（优化语音播放卡顿）
        scope.launch {
            try {
                audioUtil?.preInitializeAudio()
                Log.d(TAG, "Audio components pre-initialized successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to pre-initialize audio components", e)
            }
        }
        
        // 初始化音频播放器
        scope.launch {
            try {
                audioUtil?.initPlayer()
                Log.d(TAG, "Audio player initialized successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize audio player", e)
            }
        }
        
        Log.d(TAG, "XiaozhiService initialized with MAC address: $macAddress")
    }
    
    /**
     * 会话ID更新回调
     */
    override fun onSessionIdUpdated(sessionId: String) {
        Log.d(TAG, "Session ID updated: $sessionId")
        this.sessionId = sessionId
    }
    
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
     * 设置消息监听器
     */
    fun setMessageListener(listener: MessageListener?) {
        messageListener = listener
    }
    
    /**
     * 设置语音识别结果回调
     */
    fun setVoiceRecognitionCallback(callback: ((String) -> Unit)?) {
        voiceRecognitionCallback = callback
    }
    
    /**
     * 设置当前对话ID
     */
    fun setCurrentConversationId(conversationId: String?) {
        currentConversationId = conversationId
        Log.d(TAG, "Current conversation ID set to: $conversationId")
    }
    
    /**
     * 分发事件到所有监听器
     */
    private fun dispatchEvent(event: XiaozhiServiceEvent) {
        listeners.forEach { listener ->
            try {
                listener(event)
            } catch (e: Exception) {
                Log.e(TAG, "Error in event listener", e)
            }
        }
    }
    
    /**
     * 连接到小智服务
     */
    suspend fun connect() = withContext(Dispatchers.IO) {
        Log.d(TAG, "connect() called: isConnected=$isConnected, webSocketManager.isConnected=${webSocketManager?.isConnected()}")
        
        // 检查WebSocket管理器的连接状态
        val webSocketConnected = webSocketManager?.isConnected() ?: false
        
        // 如果WebSocket已连接但XiaozhiService状态不一致，同步状态
        if (webSocketConnected && !isConnected) {
            Log.d(TAG, "WebSocket connected but XiaozhiService not connected, syncing state")
            isConnected = true
            dispatchEvent(XiaozhiServiceEvent(XiaozhiServiceEventType.CONNECTED, null))
            return@withContext
        }
        
        if (isConnected && webSocketConnected) {
            Log.d(TAG, "Already connected, skipping connection")
            return@withContext
        }
        
        try {
            Log.d(TAG, "Connecting to Xiaozhi service...")
            webSocketManager?.connect(websocketUrl, token)
            Log.d(TAG, "Connection request sent")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to connect", e)
            dispatchEvent(XiaozhiServiceEvent(XiaozhiServiceEventType.ERROR, "连接小智服务失败: ${e.message}"))
            throw e
        }
    }
    
    /**
     * 断开连接
     */
    suspend fun disconnect() = withContext(Dispatchers.IO) {
        if (!isConnected) return@withContext
        
        try {
            webSocketManager?.disconnect()
            isConnected = false
        } catch (e: Exception) {
            Log.e(TAG, "Failed to disconnect", e)
        }
    }
    
    /**
     * 发送文本消息
     */
    suspend fun sendTextMessage(message: String, onMessageReceived: ((String) -> Unit)? = null): String = withContext(Dispatchers.IO) {
        Log.d(TAG, "sendTextMessage: isConnected=$isConnected, webSocketManager.isConnected=${webSocketManager?.isConnected()}")
        if (!isConnected) {
            Log.d(TAG, "Not connected, attempting to connect...")
            connect()
        } else {
            Log.d(TAG, "Already connected, sending message directly")
        }
        
        val completer = Completer<String>()
        
        Log.d(TAG, "Sending text message: $message")
        
        // 实时消息回调
        var messageCallback: ((String) -> Unit)? = onMessageReceived
        var isCollecting = true
        
        // 添加一次性监听器
        var onceListener: XiaozhiServiceListener? = null
        onceListener = { event ->
            when (event.type) {
                XiaozhiServiceEventType.TEXT_MESSAGE -> {
                    val response = event.data as? String ?: ""
                    if (response != message && isCollecting) {
                        Log.d(TAG, "Received response sentence (callback only): $response")
                        // 只进行回调，不处理消息存储（避免重复）
                        messageCallback?.invoke(response)
                    }
                }
                XiaozhiServiceEventType.TTS_STARTED -> {
                    Log.d(TAG, "TTS started")
                    // 预热AudioTrack，减少首句卡顿
                    scope.launch {
                        try {
                            audioUtil?.warmUpAudioTrack()
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to warm up AudioTrack", e)
                        }
                    }
                }
                XiaozhiServiceEventType.TTS_STOPPED -> {
                    Log.d(TAG, "TTS stopped")
                    isCollecting = false
                    // TTS结束，完成请求
                    if (!completer.isCompleted) {
                        completer.complete("TTS_COMPLETED")
                        onceListener?.let { removeListener(it) }
                    }
                }
                XiaozhiServiceEventType.ERROR -> {
                    if (!completer.isCompleted) {
                        Log.e(TAG, "Received error: ${event.data}")
                        completer.completeError(Exception(event.data.toString()))
                        onceListener?.let { removeListener(it) }
                    }
                }
                else -> {}
            }
        }
        
        addListener(onceListener)
        
        // 发送文本请求
        webSocketManager?.sendTextRequest(message)
        
        // 设置超时
        val timeoutJob = scope.launch {
            delay(30000) // 30秒超时，给长回复更多时间
            if (!completer.isCompleted) {
                Log.w(TAG, "Request timeout")
                completer.completeError(Exception("请求超时"))
                removeListener(onceListener)
            }
        }
        
        try {
            val result = completer.await()
            timeoutJob.cancel()
            return@withContext result
        } catch (e: Exception) {
            timeoutJob.cancel()
            throw e
        }
    }
    
    /**
     * 开始监听（按住说话模式）
     */
    suspend fun startListening(mode: String = "manual") = withContext(Dispatchers.IO) {
        if (!isConnected) {
            connect()
        }
        
        if (sessionId == null) {
            Log.w(TAG, "No session ID available")
            return@withContext
        }
        
        try {
            // 开始录音
            audioUtil?.startRecording()
            
            // 发送开始监听命令
            val message = JsonObject().apply {
                addProperty("session_id", sessionId)
                addProperty("type", "listen")
                addProperty("state", "start")
                addProperty("mode", mode)
            }
            webSocketManager?.sendMessage(gson.toJson(message))
            
            Log.d(TAG, "Started listening (hold to speak)")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start listening", e)
            throw Exception("开始语音输入失败: ${e.message}")
        }
    }
    
    /**
     * 停止监听
     */
    suspend fun stopListening() = withContext(Dispatchers.IO) {
        try {
            audioUtil?.stopRecording()
            
            if (sessionId != null) {
                val message = JsonObject().apply {
                    addProperty("session_id", sessionId)
                    addProperty("type", "listen")
                    addProperty("state", "stop")
                }
                webSocketManager?.sendMessage(gson.toJson(message))
            }
            
            Log.d(TAG, "Stopped listening")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop listening", e)
        }
    }
    
    /**
     * 取消发送
     */
    suspend fun abortListening() = withContext(Dispatchers.IO) {
        try {
            audioUtil?.stopRecording()
            
            if (sessionId != null) {
                val message = JsonObject().apply {
                    addProperty("session_id", sessionId)
                    addProperty("type", "abort")
                }
                webSocketManager?.sendMessage(gson.toJson(message))
            }
            
            Log.d(TAG, "Aborted listening")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to abort listening", e)
        }
    }
    
    /**
     * 停止播放
     */
    suspend fun stopPlayback() = withContext(Dispatchers.IO) {
        try {
            audioUtil?.stopPlaying()
            Log.d(TAG, "Playback stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop playback", e)
        }
    }
    
    /**
     * 切换静音状态
     */
    fun toggleMute() {
        isMuted = !isMuted
        
        if (webSocketManager?.isConnected() == true) {
            val request = JsonObject().apply {
                addProperty("type", if (isMuted) "voice_mute" else "voice_unmute")
            }
            webSocketManager?.sendMessage(gson.toJson(request))
        }
    }
    
    /**
     * 处理WebSocket事件
     */
    private fun onWebSocketEvent(event: XiaozhiServiceEvent) {
        when (event.type) {
            XiaozhiServiceEventType.CONNECTED -> {
                isConnected = true
                dispatchEvent(event)
            }
            XiaozhiServiceEventType.DISCONNECTED -> {
                isConnected = false
                dispatchEvent(event)
            }
            XiaozhiServiceEventType.TEXT_MESSAGE -> {
                // 只处理当前对话的消息存储，不进行回调（避免重复）
                Log.d(TAG, "Received TEXT_MESSAGE event (storage only), currentConversationId: $currentConversationId")
                if (currentConversationId != null) {
                    Log.d(TAG, "Dispatching TEXT_MESSAGE to conversation: $currentConversationId")
                    dispatchEvent(event)
                } else {
                    Log.w(TAG, "Received TEXT_MESSAGE but no current conversation ID set")
                }
            }
            XiaozhiServiceEventType.USER_MESSAGE -> {
                dispatchEvent(event)
            }
            XiaozhiServiceEventType.AUDIO_DATA -> {
                val audioData = event.data as? ByteArray
                if (audioData != null) {
                    Log.d(TAG, "Received AUDIO_DATA: ${audioData.size} bytes, audioUtil: ${audioUtil != null}")
                    scope.launch {
                        try {
                            if (audioUtil != null) {
                                audioUtil?.playOpusData(audioData)
                            } else {
                                Log.e(TAG, "AudioUtil is null, cannot play audio data")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to play audio data", e)
                        }
                    }
                } else {
                    Log.w(TAG, "Received AUDIO_DATA event but data is null")
                }
            }
            XiaozhiServiceEventType.STT_RESULT -> {
                // 处理语音识别结果
                val text = event.data as? String
                if (text != null && text.isNotEmpty()) {
                    Log.d(TAG, "Voice recognition result: $text")
                    // 调用语音识别回调
                    voiceRecognitionCallback?.invoke(text)
                    // 只处理当前对话的STT结果
                    if (currentConversationId != null) {
                        Log.d(TAG, "Dispatching STT_RESULT to conversation: $currentConversationId")
                        dispatchEvent(event)
                    } else {
                        Log.w(TAG, "Received STT_RESULT but no current conversation ID set")
                    }
                }
            }
            XiaozhiServiceEventType.STT_STARTED -> {
                Log.d(TAG, "Voice recognition started")
                dispatchEvent(event)
            }
            XiaozhiServiceEventType.STT_STOPPED -> {
                Log.d(TAG, "Voice recognition stopped")
                dispatchEvent(event)
            }
            XiaozhiServiceEventType.ERROR -> {
                dispatchEvent(event)
            }
            else -> {}
        }
    }
    
    /**
     * 检查是否已连接
     */
    fun isConnected(): Boolean = isConnected && webSocketManager?.isConnected() == true
    
    /**
     * 检查是否静音
     */
    fun isMuted(): Boolean = isMuted
    
    /**
     * 检查语音通话是否活跃
     */
    fun isVoiceCallActive(): Boolean = isVoiceCallActive
    
    /**
     * 切换到语音通话模式
     */
    suspend fun switchToVoiceCallMode() = withContext(Dispatchers.IO) {
        try {
            isVoiceCallActive = true
            // 这里可以添加切换到语音通话模式的逻辑
            Log.d(TAG, "Switched to voice call mode")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to switch to voice call mode", e)
        }
    }
    
    /**
     * 切换到聊天模式
     */
    suspend fun switchToChatMode() = withContext(Dispatchers.IO) {
        try {
            isVoiceCallActive = false
            // 这里可以添加切换到聊天模式的逻辑
            Log.d(TAG, "Switched to chat mode")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to switch to chat mode", e)
        }
    }
    
    /**
     * 断开语音通话
     */
    suspend fun disconnectVoiceCall() = withContext(Dispatchers.IO) {
        try {
            isVoiceCallActive = false
            // 这里可以添加断开语音通话的逻辑
            Log.d(TAG, "Disconnected voice call")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to disconnect voice call", e)
        }
    }
    
    /**
     * 开始实时语音流发送
     * 参考Flutter项目中的startListeningCall方法
     */
    suspend fun startVoiceStreaming() = withContext(Dispatchers.IO) {
        try {
            // 确保已经有会话ID
            if (sessionId == null) {
                Log.d(TAG, "没有会话ID，无法开始语音流，等待会话ID初始化...")
                delay(500) // 等待短暂时间
                if (sessionId == null) {
                    Log.d(TAG, "会话ID仍然为空，放弃开始语音流")
                    throw Exception("会话ID为空，无法开始语音流")
                }
            }

            Log.d(TAG, "使用会话ID开始语音流: $sessionId")

            // 确保已连接
            if (!isConnected) {
                Log.d(TAG, "未连接，尝试连接...")
                connect()
            }

            // 初始化录音器
            audioUtil?.initRecorder()

            // 开始录音
            audioUtil?.startRecording()

            // 启动音频流发送协程
            audioStreamJob = scope.launch {
                try {
                    audioUtil?.audioStream?.collect { opusData ->
                        if (isVoiceStreaming) {
                            // 发送音频数据到服务器
                            webSocketManager?.sendBinaryMessage(opusData)
                            Log.d(TAG, "Sent audio data: ${opusData.size} bytes")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Audio stream collection failed", e)
                }
            }

            // 发送开始监听命令
            webSocketManager?.startListening(sessionId, "auto")
            
            isVoiceStreaming = true
            Log.d(TAG, "语音流发送已开始")
        } catch (e: Exception) {
            Log.e(TAG, "开始语音流失败: $e")
            throw Exception("开始语音流失败: $e")
        }
    }

    /**
     * 停止实时语音流发送
     * 参考Flutter项目中的stopListeningCall方法
     */
    suspend fun stopVoiceStreaming() = withContext(Dispatchers.IO) {
        try {
            // 停止音频流发送
            audioStreamJob?.cancel()
            audioStreamJob = null

            // 停止录音
            audioUtil?.stopRecording()

            // 发送停止监听命令
            if (sessionId != null && webSocketManager != null) {
                webSocketManager?.stopListening(sessionId, "auto")
                Log.d(TAG, "已发送停止监听消息")
            }

            isVoiceStreaming = false
            Log.d(TAG, "语音流发送已停止")
        } catch (e: Exception) {
            Log.e(TAG, "停止语音流失败: $e")
        }
    }

    /**
     * 检查是否正在发送语音流
     */
    fun isVoiceStreaming(): Boolean = isVoiceStreaming

    /**
     * 重置连接状态（用于界面切换）
     */
    suspend fun resetConnectionState() = withContext(Dispatchers.IO) {
        try {
            // 停止语音流
            stopVoiceStreaming()
            
            // 清除事件监听器
            listeners.clear()
            
            // 重置状态
            isVoiceCallActive = false
            isMuted = false
            currentConversationId = null // 清除当前对话ID
            
            Log.d(TAG, "XiaozhiService connection state reset")
        } catch (e: Exception) {
            Log.e(TAG, "Error resetting connection state", e)
        }
    }
    
    /**
     * 释放资源
     */
    suspend fun dispose() = withContext(Dispatchers.IO) {
        try {
            // 停止语音流
            stopVoiceStreaming()
            
            disconnect()
            audioUtil?.dispose()
            webSocketManager?.dispose()
            scope.cancel()
            listeners.clear()
            Log.d(TAG, "XiaozhiService disposed")
        } catch (e: Exception) {
            Log.e(TAG, "Error disposing XiaozhiService", e)
        }
    }
}
