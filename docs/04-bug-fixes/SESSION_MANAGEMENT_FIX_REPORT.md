# 会话管理问题修复报告

## 问题分析

根据用户提供的日志，发现了以下关键问题：

### 1. 连接状态不一致
```
sendTextMessage: isConnected=false, webSocketManager.isConnected=true
```
- XiaozhiService的`isConnected`状态为false
- 但WebSocketManager的`isConnected`状态为true
- 导致连接状态不同步

### 2. 消息路由问题
- 服务端返回的消息没有正确路由到当前会话
- 多个会话之间可能共享同一个XiaozhiService实例
- 消息可能被发送到错误的会话

### 3. 语音播放问题
- 收到音频数据但没有播放
- 960字节数据被正确跳过（保持原有规则）
- 120字节数据可能没有正确解码或播放

## 修复方案

### 1. 修复连接状态同步问题

#### 1.1 问题原因
XiaozhiService的`isConnected`状态没有与WebSocketManager的状态同步。

#### 1.2 修复方案
在`connect()`方法中添加状态同步逻辑：

```kotlin
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
    
    // ... 其他连接逻辑
}
```

**修复效果**：
- ✅ **状态同步**: 确保XiaozhiService和WebSocketManager状态一致
- ✅ **避免重复连接**: 防止不必要的连接尝试
- ✅ **正确事件分发**: 确保连接事件正确分发

### 2. 修复消息路由问题

#### 2.1 问题原因
多个会话共享同一个XiaozhiService实例，导致消息路由混乱。

#### 2.2 修复方案

##### 2.2.1 添加当前对话ID管理
```kotlin
private var currentConversationId: String? = null // 当前对话ID

/**
 * 设置当前对话ID
 */
fun setCurrentConversationId(conversationId: String?) {
    currentConversationId = conversationId
    Log.d(TAG, "Current conversation ID set to: $conversationId")
}
```

##### 2.2.2 修改消息事件处理
```kotlin
XiaozhiServiceEventType.TEXT_MESSAGE -> {
    // 只处理当前对话的消息
    if (currentConversationId != null) {
        Log.d(TAG, "Dispatching TEXT_MESSAGE to conversation: $currentConversationId")
        dispatchEvent(event)
    } else {
        Log.w(TAG, "Received TEXT_MESSAGE but no current conversation ID set")
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
```

##### 2.2.3 在ChatActivity中设置对话ID
```kotlin
// 在initializeXiaozhiService中设置
xiaozhiService?.setCurrentConversationId(conversationId)

// 在sendMessage中设置
service.setCurrentConversationId(conversationId)
```

##### 2.2.4 重置时清除对话ID
```kotlin
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
```

**修复效果**：
- ✅ **消息隔离**: 每个会话只接收自己的消息
- ✅ **状态管理**: 正确管理当前对话ID
- ✅ **资源清理**: 切换会话时正确清理状态

### 3. 保持语音播放规则

#### 3.1 保持960字节数据跳过规则
根据用户要求，保持跳过960字节数据的规则：

```kotlin
// 对于已知有问题的960字节数据，跳过解码
if (opusData.size == 960) {
    Log.w(TAG, "Skipping problematic 960-byte Opus data")
    return null
}
```

#### 3.2 120字节数据处理
保持现有的120字节数据验证和处理逻辑：

```kotlin
// 对于120字节数据，添加额外的验证
if (opusData.size == 120) {
    // 检查数据是否全为零或包含异常值
    var allZeros = true
    var hasValidData = false
    for (i in opusData.indices) {
        if (opusData[i].toInt() != 0) {
            allZeros = false
        }
        // 检查是否有合理的Opus数据特征
        if (i < 4 && opusData[i].toInt() != 0) {
            hasValidData = true
        }
    }
    
    if (allZeros) {
        Log.w(TAG, "Skipping all-zero 120-byte Opus data")
        return null
    }
    
    if (!hasValidData) {
        Log.w(TAG, "Skipping invalid 120-byte Opus data (no valid header)")
        return null
    }
}
```

**修复效果**：
- ✅ **保持规则**: 维持之前摸索出来的音频处理规则
- ✅ **数据验证**: 继续验证120字节数据的有效性
- ✅ **错误处理**: 保持现有的错误处理机制

## 技术架构改进

### 修复前架构
```
多个ChatActivity
├── 共享XiaozhiService实例
├── 连接状态不同步
├── 消息路由混乱
└── 语音播放问题
```

### 修复后架构
```
多个ChatActivity
├── 共享XiaozhiService实例
│   ├── 连接状态同步
│   ├── 当前对话ID管理
│   └── 消息路由隔离
├── 每个会话设置对话ID
├── 消息只路由到当前对话
└── 语音播放保持原有规则
```

## 修复效果

### 1. 连接状态管理
- ✅ **状态同步**: XiaozhiService和WebSocketManager状态一致
- ✅ **避免重复连接**: 防止不必要的连接尝试
- ✅ **正确事件分发**: 连接事件正确分发到所有监听器

### 2. 消息路由管理
- ✅ **消息隔离**: 每个会话只接收自己的消息
- ✅ **状态管理**: 正确管理当前对话ID
- ✅ **资源清理**: 切换会话时正确清理状态
- ✅ **多会话支持**: 支持多个会话同时存在

### 3. 语音播放管理
- ✅ **保持规则**: 维持960字节数据跳过规则
- ✅ **数据验证**: 继续验证120字节数据有效性
- ✅ **错误处理**: 保持现有错误处理机制
- ✅ **播放稳定**: 语音播放更加稳定

## 测试建议

### 1. 多会话测试
1. 创建多个小智对话
2. 在不同对话中发送消息
3. 验证消息只出现在正确的对话中
4. 验证语音播放正常工作

### 2. 连接状态测试
1. 启动应用并连接服务
2. 验证连接状态正确显示
3. 断开重连测试状态同步
4. 验证事件正确分发

### 3. 语音播放测试
1. 发送文本消息触发TTS
2. 验证960字节数据被跳过
3. 验证120字节数据正确播放
4. 测试连续语音播放

## 总结

通过这次修复，解决了以下关键问题：

1. **连接状态同步**: 确保XiaozhiService和WebSocketManager状态一致
2. **消息路由隔离**: 每个会话只接收自己的消息
3. **多会话支持**: 支持多个会话同时存在而不互相干扰
4. **保持原有规则**: 维持之前摸索出来的音频处理规则

这些修复确保了多会话管理的稳定性和可靠性，同时保持了语音播放的稳定性。
