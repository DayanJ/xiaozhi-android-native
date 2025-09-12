# 事件流调试报告

## 问题分析

根据用户提供的日志，我发现了关键问题：

### 1. 事件流中断
**现象**：
- 收到了TTS消息：`"你好呀😃"` 和 `"今天过得咋样~有没有什么新鲜事儿想和我聊聊👂"`
- 收到了大量音频数据（960字节和120字节）
- 但没有看到我添加的调试日志

**关键发现**：
- 没有看到 `Dispatching TEXT_MESSAGE for sentence_start` 日志
- 没有看到 `Dispatching AUDIO_DATA event` 日志
- 没有看到 `Dispatching event: TEXT_MESSAGE, listeners count: X` 日志
- 没有看到 `Received TEXT_MESSAGE event, currentConversationId: X` 日志

**结论**：事件没有从XiaozhiWebSocketManager正确分发到XiaozhiService

## 调试日志添加

### 1. XiaozhiWebSocketManager事件分发调试

#### 1.1 dispatchEvent方法调试
```kotlin
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
```

**调试效果**：
- ✅ **事件追踪**: 记录每个事件的类型
- ✅ **监听器检查**: 显示监听器数量
- ✅ **错误处理**: 捕获监听器执行错误

#### 1.2 TTS消息处理调试
```kotlin
"sentence_start" -> {
    if (text.isNotEmpty()) {
        Log.d(TAG, "Dispatching TEXT_MESSAGE for sentence_start: $text")
        dispatchEvent(XiaozhiServiceEvent(XiaozhiServiceEventType.TEXT_MESSAGE, text))
    } else {
        Log.w(TAG, "TTS sentence_start but text is empty")
    }
}
```

**调试效果**：
- ✅ **消息追踪**: 记录TTS消息内容
- ✅ **分发确认**: 确认TEXT_MESSAGE事件分发
- ✅ **空值检查**: 检查文本是否为空

#### 1.3 音频数据处理调试
```kotlin
override fun onMessage(webSocket: WebSocket, bytes: okio.ByteString) {
    Log.d(TAG, "Received binary message: ${bytes.size} bytes")
    Log.d(TAG, "Dispatching AUDIO_DATA event for ${bytes.size} bytes")
    dispatchEvent(XiaozhiServiceEvent(XiaozhiServiceEventType.AUDIO_DATA, bytes.toByteArray()))
}
```

**调试效果**：
- ✅ **数据追踪**: 记录音频数据大小
- ✅ **事件分发**: 确认AUDIO_DATA事件分发
- ✅ **流程监控**: 监控音频数据处理流程

### 2. XiaozhiService事件接收调试

#### 2.1 TEXT_MESSAGE事件调试
```kotlin
XiaozhiServiceEventType.TEXT_MESSAGE -> {
    Log.d(TAG, "Received TEXT_MESSAGE event, currentConversationId: $currentConversationId")
    if (currentConversationId != null) {
        Log.d(TAG, "Dispatching TEXT_MESSAGE to conversation: $currentConversationId")
        dispatchEvent(event)
    } else {
        Log.w(TAG, "Received TEXT_MESSAGE but no current conversation ID set")
    }
}
```

#### 2.2 AUDIO_DATA事件调试
```kotlin
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
```

## 期望的日志输出

### 1. 正常事件流日志
```
// TTS消息处理
Received TTS message - state: sentence_start, text: 你好呀😃
Dispatching TEXT_MESSAGE for sentence_start: 你好呀😃
Dispatching event: TEXT_MESSAGE, listeners count: 1
Received TEXT_MESSAGE event, currentConversationId: 44b52548-1c85-43ac-9068-98c45c68dae4
Dispatching TEXT_MESSAGE to conversation: 44b52548-1c85-43ac-9068-98c45c68dae4

// 音频数据处理
Received binary message: 120 bytes
Dispatching AUDIO_DATA event for 120 bytes
Dispatching event: AUDIO_DATA, listeners count: 1
Received AUDIO_DATA: 120 bytes, audioUtil: true
Playing Opus data: 120 bytes
```

### 2. 问题诊断日志
```
// 如果监听器为空
Dispatching event: TEXT_MESSAGE, listeners count: 0

// 如果事件没有分发
// 没有看到 "Dispatching TEXT_MESSAGE for sentence_start" 日志

// 如果AudioUtil为null
Received AUDIO_DATA: 120 bytes, audioUtil: false
AudioUtil is null, cannot play audio data
```

## 问题诊断流程

### 1. 检查事件分发
1. **查看TTS消息处理**：
   - 是否有 `Dispatching TEXT_MESSAGE for sentence_start` 日志
   - 如果没有，说明TTS消息处理有问题

2. **查看事件分发**：
   - 是否有 `Dispatching event: TEXT_MESSAGE, listeners count: X` 日志
   - 如果listeners count为0，说明监听器没有添加

3. **查看事件接收**：
   - 是否有 `Received TEXT_MESSAGE event` 日志
   - 如果没有，说明事件没有到达XiaozhiService

### 2. 检查音频处理
1. **查看音频数据接收**：
   - 是否有 `Dispatching AUDIO_DATA event` 日志
   - 如果没有，说明音频数据处理有问题

2. **查看事件分发**：
   - 是否有 `Dispatching event: AUDIO_DATA, listeners count: X` 日志
   - 如果listeners count为0，说明监听器没有添加

3. **查看音频播放**：
   - 是否有 `Received AUDIO_DATA` 日志
   - 如果没有，说明事件没有到达XiaozhiService

## 可能的问题原因

### 1. 监听器没有添加
- XiaozhiService的`onWebSocketEvent`方法没有正确添加到XiaozhiWebSocketManager
- 监听器列表为空

### 2. 事件分发失败
- dispatchEvent方法没有被调用
- 事件分发过程中出现异常

### 3. 事件处理失败
- XiaozhiService的事件处理器没有正确执行
- 事件类型不匹配

## 下一步行动

### 1. 运行测试
1. 重新运行应用
2. 发送文本消息
3. 观察新的调试日志输出

### 2. 根据日志结果诊断
- **如果看到所有调试日志**：说明事件流正常，问题在其他地方
- **如果只看到部分日志**：根据缺失的日志确定问题点
- **如果看不到任何新日志**：说明代码没有更新或编译失败

### 3. 修复问题
根据日志结果进行相应的修复：
- 监听器问题 → 检查监听器添加逻辑
- 事件分发问题 → 检查dispatchEvent调用
- 事件处理问题 → 检查事件处理器逻辑

## 总结

通过添加详细的事件流调试日志，我们可以：

1. **精确定位问题**：确定事件在哪个环节中断
2. **监控事件流**：跟踪事件从WebSocket到Service的完整流程
3. **检查监听器**：确认事件监听器是否正确添加
4. **诊断错误**：快速识别和定位问题

这些调试日志将帮助我们快速找到事件流中断的根本原因。请运行应用并观察新的日志输出，然后根据日志结果进行相应的修复。
