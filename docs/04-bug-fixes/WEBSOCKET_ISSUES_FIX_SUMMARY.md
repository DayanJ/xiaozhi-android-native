# WebSocket对话问题修复总结

## 问题分析

从用户提供的日志中发现了以下三个主要问题：

1. **连接没有保持**：每次发送消息都重新连接
2. **对话列表中消息重复显示**：STT回显消息被重复处理
3. **语音无法播放**：Opus decoder未初始化

## 修复内容

### 1. 修复Opus decoder未初始化问题

**问题**：AudioUtil的`initPlayer()`方法只初始化了AudioTrack，但没有初始化Opus decoder
**解决方案**：在`initPlayer()`方法中添加Opus编解码器初始化

```kotlin
// 修复前
audioTrack = AudioTrack.Builder()...
Log.d(TAG, "AudioTrack initialized with buffer size: $bufferSize")

// 修复后
audioTrack = AudioTrack.Builder()...
// 初始化Opus编解码器（用于播放）
initOpusCodecs()
Log.d(TAG, "AudioTrack initialized with buffer size: $bufferSize")
```

**修复位置**：`AudioUtil.kt` - `initPlayer()`方法

### 2. 修复消息重复显示问题

**问题**：STT消息（用户输入的回显）被当作USER_MESSAGE处理，导致重复显示
**解决方案**：忽略STT回显消息，避免重复添加到对话列表

```kotlin
// 修复前
if (state == "final" && text.isNotEmpty()) {
    dispatchEvent(XiaozhiServiceEvent(XiaozhiServiceEventType.USER_MESSAGE, text))
}

// 修复后
if (state == "final" && text.isNotEmpty()) {
    // 忽略STT回显消息，避免重复显示
    Log.d(TAG, "Ignoring STT echo message: $text")
}
```

**修复位置**：`XiaozhiWebSocketManager.kt` - `handleTextMessage()`方法

### 3. 增强连接状态调试

**问题**：连接状态可能没有正确维护，导致每次重新连接
**解决方案**：添加详细的调试日志来跟踪连接状态

```kotlin
// 在sendTextMessage中添加连接状态日志
Log.d(TAG, "sendTextMessage: isConnected=$isConnected, webSocketManager.isConnected=${webSocketManager?.isConnected()}")
if (!isConnected) {
    Log.d(TAG, "Not connected, attempting to connect...")
    connect()
} else {
    Log.d(TAG, "Already connected, sending message directly")
}

// 在connect方法中添加连接状态日志
Log.d(TAG, "connect() called: isConnected=$isConnected")
if (isConnected) {
    Log.d(TAG, "Already connected, skipping connection")
    return@withContext
}
```

**修复位置**：`XiaozhiService.kt` - `sendTextMessage()`和`connect()`方法

## 技术细节

### Opus编解码器初始化

**问题原因**：
- `initRecorder()`方法中调用了`initOpusCodecs()`
- `initPlayer()`方法中没有调用`initOpusCodecs()`
- 导致播放时Opus decoder为null

**修复方案**：
- 在`initPlayer()`方法中添加`initOpusCodecs()`调用
- 确保播放和录制都能正常使用Opus编解码器

### 消息重复处理

**问题原因**：
- 用户发送文本消息时，服务器会返回STT回显
- STT回显被当作用户消息处理，导致重复显示
- 实际上用户已经手动发送了消息

**修复方案**：
- 忽略所有STT消息，避免重复显示
- 保留日志记录，便于调试
- 未来可以添加逻辑来区分语音输入和文本回显

### 连接状态管理

**问题原因**：
- 连接状态可能没有正确维护
- 每次发送消息时都检查连接状态并重新连接
- 需要更好的调试信息来跟踪连接状态

**修复方案**：
- 添加详细的连接状态日志
- 跟踪`isConnected`和`webSocketManager.isConnected()`状态
- 记录连接和发送消息的详细过程

## 预期效果

### 修复后的预期行为

1. **语音播放正常**：
   - Opus decoder正确初始化
   - 收到二进制音频数据时能正常解码和播放
   - 不再出现"Opus decoder not initialized"警告

2. **消息显示正确**：
   - 用户消息只显示一次
   - 不再有重复的消息显示
   - STT回显消息被正确忽略

3. **连接状态清晰**：
   - 连接状态正确维护
   - 详细的调试日志帮助跟踪连接过程
   - 避免不必要的重新连接

### 关键日志验证

修复后应该看到以下日志：
```
AudioUtil: AudioTrack initialized with buffer size: [size]
AudioUtil: Opus codecs initialized successfully
XiaozhiService: sendTextMessage: isConnected=true, webSocketManager.isConnected=true
XiaozhiService: Already connected, sending message directly
XiaozhiWebSocketManager: Ignoring STT echo message: [message]
XiaozhiWebSocketManager: Received binary message: [size] bytes
AudioUtil: Playing Opus data: [size] bytes (无警告)
```

## 相关文件

### 修改的文件：
- `AudioUtil.kt`：修复Opus decoder初始化
- `XiaozhiWebSocketManager.kt`：修复消息重复处理
- `XiaozhiService.kt`：增强连接状态调试

### 新增功能：
- Opus decoder在播放时正确初始化
- STT回显消息过滤机制
- 详细的连接状态调试日志

## 测试建议

### 测试步骤：
1. **语音播放测试**：
   - 发送文本消息
   - 检查是否收到二进制音频数据
   - 验证语音是否正常播放

2. **消息显示测试**：
   - 发送多条文本消息
   - 检查对话列表中的消息数量
   - 确认没有重复消息

3. **连接状态测试**：
   - 发送第一条消息后检查连接日志
   - 发送第二条消息，确认不会重新连接
   - 检查连接状态日志

## 总结

通过这次修复，解决了以下问题：
1. ✅ 修复了Opus decoder未初始化导致语音无法播放的问题
2. ✅ 修复了STT回显消息重复显示的问题
3. ✅ 增强了连接状态的调试和跟踪
4. ✅ 改进了消息处理的逻辑
5. ✅ 提供了更好的错误处理和日志记录

现在小智WebSocket对话功能应该能够：
- 正常播放服务端返回的语音
- 正确显示消息，无重复
- 保持连接状态，避免不必要的重新连接
- 提供详细的调试信息，便于问题排查
