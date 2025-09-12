# Opus音频编解码集成指南

## 概述
本指南说明如何在Android原生项目中集成Opus音频编解码功能，以匹配Flutter项目中的`opus_dart`和`opus_flutter`功能。

## 当前状态
✅ **基础PCM音频处理** - 已实现  
⏳ **Opus编解码** - 待集成  

## 推荐的Opus库选择

### 1. **首选方案：ExoPlayer + Opus扩展**
```gradle
implementation 'com.google.android.exoplayer:exoplayer:2.19.1'
implementation 'com.google.android.exoplayer:extension-opus:2.19.1'
```

**优势：**
- Google官方维护，稳定可靠
- 支持Opus格式的音频播放
- 与Android MediaPlayer API兼容
- 性能优秀

**适用场景：** 主要用于音频播放，不适合实时编解码

### 2. **实时编解码方案：FFmpeg Kit**
```gradle
implementation 'com.arthenica:ffmpeg-kit-audio:6.0-2'
```

**优势：**
- 功能最全面
- 支持多种音频格式
- 支持实时编解码
- 性能最佳

**适用场景：** 需要实时Opus编解码的场景

### 3. **轻量级方案：WebRTC**
```gradle
implementation 'org.webrtc:google-webrtc:1.0.32006'
```

**优势：**
- 专为实时通信优化
- 低延迟
- 适合语音通话场景

**适用场景：** 实时语音通话

## 集成步骤

### 步骤1：添加依赖
在`app/build.gradle`中添加选择的Opus库：

```gradle
dependencies {
    // 选择其中一个方案
    implementation 'com.google.android.exoplayer:exoplayer:2.19.1'
    implementation 'com.google.android.exoplayer:extension-opus:2.19.1'
    
    // 或者
    // implementation 'com.arthenica:ffmpeg-kit-audio:6.0-2'
    
    // 或者
    // implementation 'org.webrtc:google-webrtc:1.0.32006'
}
```

### 步骤2：更新AudioUtil.kt
根据选择的库，更新`AudioUtil.kt`中的以下方法：

1. **导入语句**
```kotlin
// ExoPlayer方案
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource

// FFmpeg方案
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode

// WebRTC方案
import org.webrtc.audio.AudioTrack
```

2. **初始化方法**
```kotlin
private fun initOpusCodecs() {
    // 根据选择的库实现初始化逻辑
}
```

3. **编码方法**
```kotlin
private fun encodeToOpus(pcmData: ByteArray): ByteArray? {
    // 实现PCM到Opus的编码
}
```

4. **解码方法**
```kotlin
private fun decodeFromOpus(opusData: ByteArray): ByteArray? {
    // 实现Opus到PCM的解码
}
```

### 步骤3：配置参数
确保与Flutter项目保持一致的参数：

```kotlin
companion object {
    private const val SAMPLE_RATE = 16000        // 采样率
    private const val CHANNELS = 1               // 单声道
    private const val FRAME_DURATION_MS = 60     // 帧长度
    private const val BITRATE = 64000            // 比特率
}
```

## 与Flutter项目的对应关系

| Flutter项目 | Android原生项目 | 说明 |
|------------|----------------|------|
| `opus_dart` | Concentus/ExoPlayer/FFmpeg | Opus编解码库 |
| `SimpleOpusEncoder` | `OpusEncoder` | Opus编码器 |
| `SimpleOpusDecoder` | `OpusDecoder` | Opus解码器 |
| `encodeToOpus()` | `encodeToOpus()` | PCM编码为Opus |
| `playOpusData()` | `playOpusData()` | 播放Opus数据 |
| `flutter_sound` | `AudioRecord`/`AudioTrack` | 音频录制播放 |

## 测试验证

### 1. 编译测试
```bash
gradle assembleDebug -x lint
```

### 2. 功能测试
- 录音功能测试
- Opus编码测试
- Opus解码测试
- 音频播放测试

### 3. 性能测试
- 编码延迟测试
- 解码延迟测试
- 内存使用测试
- CPU使用测试

## 注意事项

1. **权限要求**
   - `RECORD_AUDIO` - 录音权限
   - `MODIFY_AUDIO_SETTINGS` - 音频设置权限

2. **线程安全**
   - 音频处理应在IO线程进行
   - 使用协程处理异步操作

3. **错误处理**
   - 添加适当的异常处理
   - 记录详细的错误日志

4. **资源管理**
   - 及时释放音频资源
   - 避免内存泄漏

## 后续优化

1. **性能优化**
   - 使用原生库提高性能
   - 优化内存使用

2. **功能扩展**
   - 支持更多音频格式
   - 添加音频效果处理

3. **稳定性提升**
   - 增强错误处理
   - 添加重试机制

---

**当前实现状态：** 基础PCM音频处理已完成，Opus编解码功能待集成。  
**下一步：** 根据项目需求选择合适的Opus库并完成集成。

