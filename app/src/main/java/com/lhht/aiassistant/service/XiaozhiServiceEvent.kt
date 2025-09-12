package com.lhht.aiassistant.service

/**
 * 小智服务事件类型
 */
enum class XiaozhiServiceEventType {
    CONNECTED,
    DISCONNECTED,
    TEXT_MESSAGE,
    AUDIO_DATA,
    ERROR,
    VOICE_CALL_START,
    VOICE_CALL_END,
    USER_MESSAGE,
    TTS_STARTED,
    TTS_STOPPED,
    STT_RESULT,  // 语音识别结果
    STT_STARTED, // 语音识别开始
    STT_STOPPED  // 语音识别结束
}

/**
 * 小智服务事件
 */
data class XiaozhiServiceEvent(
    val type: XiaozhiServiceEventType,
    val data: Any?
)

/**
 * 小智服务监听器
 */
typealias XiaozhiServiceListener = (XiaozhiServiceEvent) -> Unit

/**
 * 消息监听器
 */
typealias MessageListener = (Any) -> Unit
