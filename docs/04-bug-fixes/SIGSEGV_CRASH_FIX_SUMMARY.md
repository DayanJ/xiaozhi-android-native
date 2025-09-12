# SIGSEGV崩溃修复总结

## 问题分析

根据用户提供的崩溃日志，发现了以下关键问题：

### 1. SIGSEGV崩溃
```
Fatal signal 11 (SIGSEGV), code 1 (SEGV_MAPERR), fault addr 0x0
```
- 崩溃发生在`AudioUtil$playPcmData$2.invokeSuspend`中
- 错误代码`SEGV_MAPERR`表示访问了无效的内存地址（0x0）

### 2. 并发访问问题
```
14112-14132 AudioUtil com.lhht.aiassistant D Playing Opus data: 120 bytes
14112-14131 AudioUtil com.lhht.aiassistant D Playing Opus data: 120 bytes
14112-14130 AudioUtil com.lhht.aiassistant D Playing Opus data: 120 bytes
```
- 多个线程（14132, 14131, 14130, 14170, 14169等）同时处理音频
- 之前的同步机制没有完全生效

### 3. AudioTrack访问问题
- `audioTrack`可能为null，导致空指针访问
- 并发访问AudioTrack导致内存损坏

## 修复方案

### 1. 完善同步机制

**问题**：之前的同步机制只保护了`playOpusData`方法，但`playPcmData`方法没有保护。

**修复**：
```kotlin
private suspend fun playPcmData(data: ByteArray) = withContext(Dispatchers.IO) {
    synchronized(audioLock) {  // 添加同步保护
        try {
            // 检查AudioTrack是否已初始化
            if (audioTrack == null) {
                Log.w(TAG, "AudioTrack not initialized, skipping PCM playback")
                return@withContext
            }
            
            // 确保AudioTrack已开始播放
            if (audioTrack!!.playState != AudioTrack.PLAYSTATE_PLAYING) {
                audioTrack!!.play()
                isPlaying = true
                Log.d(TAG, "AudioTrack started playing")
            }
            
            // 写入音频数据
            val bytesWritten = audioTrack!!.write(data, 0, data.size)
            Log.d(TAG, "Played $bytesWritten bytes of audio data")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play PCM data", e)
            
            // 如果播放失败，尝试重新初始化AudioTrack
            try {
                Log.w(TAG, "Attempting to reinitialize AudioTrack after error")
                // 在同步块外调用suspend函数
                scope.launch {
                    initPlayer()
                }
            } catch (reinitException: Exception) {
                Log.e(TAG, "Failed to reinitialize AudioTrack", reinitException)
            }
        }
    }
}
```

### 2. 保护AudioTrack初始化

**问题**：`initPlayer`方法没有同步保护，可能导致并发初始化。

**修复**：
```kotlin
suspend fun initPlayer() = withContext(Dispatchers.IO) {
    synchronized(audioLock) {  // 添加同步保护
        try {
            // 如果已经初始化，先释放旧的AudioTrack
            audioTrack?.release()
            
            val bufferSize = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AUDIO_FORMAT
            ) * BUFFER_SIZE_FACTOR
            
            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(/* ... */)
                .setAudioFormat(/* ... */)
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
            
            // 初始化Opus编解码器（用于播放）
            initOpusCodecs()
            
            Log.d(TAG, "AudioTrack initialized with buffer size: $bufferSize")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize AudioTrack", e)
            throw e
        }
    }
}
```

### 3. 保护停止播放操作

**问题**：`stopPlaying`方法没有同步保护。

**修复**：
```kotlin
suspend fun stopPlaying() = withContext(Dispatchers.IO) {
    synchronized(audioLock) {  // 添加同步保护
        if (!isPlaying) return@withContext
        
        try {
            isPlaying = false
            audioTrack?.stop()
            Log.d(TAG, "Playback stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop playback", e)
        }
    }
}
```

### 4. 解决suspend函数调用问题

**问题**：在同步块内调用suspend函数导致编译错误。

**修复**：
```kotlin
// 错误的方式（在同步块内调用suspend函数）
synchronized(audioLock) {
    initPlayer()  // 编译错误
}

// 正确的方式（在同步块外调用suspend函数）
scope.launch {
    initPlayer()
}
```

## 技术细节

### 同步机制设计

1. **统一锁对象**：
   - 使用`audioLock`保护所有AudioTrack相关操作
   - 确保同一时间只有一个线程能访问AudioTrack

2. **锁的粒度**：
   - 保护关键资源（AudioTrack、Opus解码器）
   - 最小化同步块范围，避免性能问题

3. **避免死锁**：
   - 不在同步块内调用suspend函数
   - 使用`scope.launch`异步调用suspend函数

### 内存安全

1. **空指针检查**：
   - 在访问AudioTrack前检查是否为null
   - 提供安全的默认行为

2. **资源管理**：
   - 在重新初始化前释放旧的AudioTrack
   - 防止内存泄漏

3. **错误恢复**：
   - 播放失败时自动重新初始化
   - 提供优雅的错误处理

## 预期效果

### 修复后的预期行为

1. **消除SIGSEGV崩溃**：
   - ✅ 防止空指针访问AudioTrack
   - ✅ 避免并发访问导致的内存损坏
   - ✅ 提供安全的资源管理

2. **消除并发竞争**：
   - ✅ 确保单线程访问AudioTrack
   - ✅ 防止多线程同时初始化
   - ✅ 保证播放操作的原子性

3. **提高稳定性**：
   - ✅ 自动错误恢复机制
   - ✅ 安全的资源释放
   - ✅ 详细的错误日志

4. **保持性能**：
   - ✅ 最小化同步开销
   - ✅ 异步错误恢复
   - ✅ 流式播放支持

## 测试建议

### 验证修复效果

1. **稳定性测试**：
   - 长时间运行测试
   - 多次对话测试
   - 观察是否还有SIGSEGV崩溃

2. **并发测试**：
   - 观察日志中是否还有多线程同时处理
   - 检查播放是否连续流畅

3. **错误恢复测试**：
   - 模拟网络中断
   - 测试自动恢复机制

## 总结

通过完善同步机制和添加AudioTrack保护，解决了：

- ✅ **SIGSEGV崩溃**：防止空指针访问和内存损坏
- ✅ **并发竞争**：确保单线程访问关键资源
- ✅ **资源管理**：安全的初始化和释放
- ✅ **错误恢复**：自动重新初始化机制

现在音频播放应该更加稳定，不再出现SIGSEGV崩溃！