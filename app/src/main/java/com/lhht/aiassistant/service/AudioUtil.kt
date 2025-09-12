package com.lhht.aiassistant.service

import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.nio.ByteBuffer
import java.nio.ByteOrder
import io.github.jaredmdobson.concentus.OpusEncoder
import io.github.jaredmdobson.concentus.OpusDecoder
import io.github.jaredmdobson.concentus.OpusApplication

/**
 * 音频工具类
 */
class AudioUtil(private val context: Context) {
    
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // 添加同步锁，防止并发访问
    private val audioLock = Any()
    private val decoderLock = Any()
    
    companion object {
        private const val TAG = "AudioUtil"
        private const val SAMPLE_RATE = 16000
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private const val BUFFER_SIZE_FACTOR = 4 // 增加缓冲区大小，减少卡顿
        private const val FRAME_DURATION_MS = 60 // 与Flutter项目保持一致
        
        @Volatile
        private var INSTANCE: AudioUtil? = null
        
        fun getInstance(context: Context): AudioUtil {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AudioUtil(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null
    private var isRecording = false
    private var isPlaying = false
    
    private val _audioStream = MutableSharedFlow<ByteArray>()
    val audioStream: SharedFlow<ByteArray> = _audioStream.asSharedFlow()
    
    private val recordingJob: Job? = null
    private val playbackJob: Job? = null
    
    // Opus编解码器（使用Concentus库）
    private var opusEncoder: OpusEncoder? = null
    private var opusDecoder: OpusDecoder? = null
    
    /**
     * 初始化Opus编解码器
     */
    private fun initOpusCodecs() {
        try {
            // 初始化Opus编码器
            opusEncoder = OpusEncoder(
                SAMPLE_RATE,
                1, // 单声道
                OpusApplication.OPUS_APPLICATION_VOIP
            )
            
            // 初始化Opus解码器
            opusDecoder = OpusDecoder(
                SAMPLE_RATE,
                1 // 单声道
            )
            
            Log.d(TAG, "Opus codecs initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Opus codecs", e)
        }
    }
    
    /**
     * 初始化录音器
     */
    suspend fun initRecorder() = withContext(Dispatchers.IO) {
        try {
            val bufferSize = AudioRecord.getMinBufferSize(
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT
            ) * BUFFER_SIZE_FACTOR
            
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                bufferSize
            )
            
            // 初始化Opus编解码器
            initOpusCodecs()
            
            Log.d(TAG, "AudioRecord initialized with buffer size: $bufferSize")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize AudioRecord", e)
            throw e
        }
    }
    
    /**
     * 预初始化音频组件（优化语音播放卡顿）
     */
    suspend fun preInitializeAudio() = withContext(Dispatchers.IO) {
        synchronized(audioLock) {
            try {
                // 预初始化Opus解码器
                if (opusDecoder == null) {
                    Log.d(TAG, "Pre-initializing Opus decoder")
                    initOpusCodecs()
                }
                
                // 预初始化AudioTrack
                if (audioTrack == null) {
                    Log.d(TAG, "Pre-initializing AudioTrack")
                    initAudioTrack()
                }
                
                Log.d(TAG, "Audio components pre-initialized successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to pre-initialize audio components", e)
            }
        }
    }
    
    /**
     * 预热AudioTrack（在TTS开始时调用，减少首句卡顿）
     */
    suspend fun warmUpAudioTrack() = withContext(Dispatchers.IO) {
        synchronized(audioLock) {
            try {
                if (audioTrack != null && audioTrack!!.playState != AudioTrack.PLAYSTATE_PLAYING) {
                    // 写入少量静音数据来预热AudioTrack
                    val silenceData = ByteArray(1024) // 1KB的静音数据
                    audioTrack!!.write(silenceData, 0, silenceData.size)
                    audioTrack!!.play()
                    Log.d(TAG, "AudioTrack warmed up with silence data")
                } else {
                    Log.d(TAG, "AudioTrack already playing or not initialized")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to warm up AudioTrack", e)
            }
        }
    }
    
    /**
     * 初始化AudioTrack（提取为独立方法）
     */
    private fun initAudioTrack() {
        try {
            val bufferSize = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AUDIO_FORMAT
            ) * BUFFER_SIZE_FACTOR
            
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            
            // 使用更兼容的AudioTrack初始化方式
            audioTrack = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                AudioTrack.Builder()
                    .setAudioAttributes(
                        android.media.AudioAttributes.Builder()
                            .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                            .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AUDIO_FORMAT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
            } else {
                // 兼容旧版本Android
                AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AUDIO_FORMAT,
                    bufferSize,
                    AudioTrack.MODE_STREAM
                )
            }
            
            Log.d(TAG, "AudioTrack initialized with buffer size: $bufferSize")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize AudioTrack", e)
            throw e
        }
    }
    
    /**
     * 初始化播放器
     */
    suspend fun initPlayer() = withContext(Dispatchers.IO) {
        synchronized(audioLock) {
            try {
                // 如果已经初始化，先释放旧的AudioTrack
                audioTrack?.release()
                
                // 使用统一的AudioTrack初始化方法
                initAudioTrack()
                
                // 设置AudioTrack使用系统音量流
                try {
                    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                    // 对于API 23+，我们可以设置音频会话ID来关联到特定的音频流
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        val sessionId = audioManager.generateAudioSessionId()
                        // 使用反射调用setAudioSessionId方法，因为它在某些版本中可能不可用
                        try {
                            val method = audioTrack?.javaClass?.getMethod("setAudioSessionId", Int::class.java)
                            method?.invoke(audioTrack, sessionId)
                            Log.d(TAG, "Audio session ID set to: $sessionId")
                        } catch (e: Exception) {
                            Log.w(TAG, "Failed to set audio session ID via reflection", e)
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to set audio session ID", e)
                }
                
                // 初始化Opus编解码器（用于播放）
                initOpusCodecs()
                
                Log.d(TAG, "AudioTrack initialized successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize AudioTrack", e)
                throw e
            }
        }
    }
    
    /**
     * 开始录音
     * 优化为实时流式录音，参考Flutter项目的流式录音实现
     */
    suspend fun startRecording() = withContext(Dispatchers.IO) {
        if (isRecording) return@withContext
        
        try {
            audioRecord?.startRecording()
            isRecording = true
            
            // 启动实时录音协程 - 使用更小的缓冲区以降低延迟
            CoroutineScope(Dispatchers.IO).launch {
                // 使用60ms帧长度的缓冲区，与Flutter项目保持一致
                val frameSize = (SAMPLE_RATE * FRAME_DURATION_MS) / 1000 * 2 // 16位 = 2字节
                val buffer = ByteArray(frameSize)
                
                Log.d(TAG, "Starting real-time recording with frame size: $frameSize bytes")
                
                while (isRecording && audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    val bytesRead = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    if (bytesRead > 0) {
                        val audioData = buffer.copyOf(bytesRead)
                        
                        // 实时编码并发送Opus数据
                        val opusData = encodeToOpus(audioData)
                        if (opusData != null) {
                            _audioStream.emit(opusData)
                            Log.d(TAG, "Emitted audio frame: ${opusData.size} bytes")
                        }
                    }
                }
                
                Log.d(TAG, "Recording loop ended")
            }
            
            Log.d(TAG, "Real-time recording started")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start recording", e)
            throw e
        }
    }
    
    /**
     * 停止录音
     */
    suspend fun stopRecording() = withContext(Dispatchers.IO) {
        if (!isRecording) return@withContext
        
        try {
            isRecording = false
            audioRecord?.stop()
            Log.d(TAG, "Recording stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop recording", e)
        }
    }
    
    /**
     * 将PCM数据编码为Opus格式
     * 优化为实时流式编码，参考Flutter项目的encodeToOpus方法
     */
    private fun encodeToOpus(pcmData: ByteArray): ByteArray? {
        return try {
            if (opusEncoder == null) {
                Log.w(TAG, "Opus encoder not initialized")
                return null
            }
            
            // 基本验证
            if (pcmData.isEmpty()) {
                return null
            }
            
            // 转换PCM数据为Int16数组（小端字节序）
            val pcmInt16 = ShortArray(pcmData.size / 2)
            val buffer = ByteBuffer.wrap(pcmData).order(ByteOrder.LITTLE_ENDIAN)
            for (i in pcmInt16.indices) {
                pcmInt16[i] = buffer.short
            }
            
            // 计算每帧的样本数（60ms帧长度）
            val samplesPerFrame = (SAMPLE_RATE * FRAME_DURATION_MS) / 1000
            
            // 准备编码缓冲区
            val encodedBuffer = ByteArray(4000) // Opus最大帧大小
            
            val encodedBytes = when {
                pcmInt16.size == samplesPerFrame -> {
                    // 精确的帧长度，直接编码
                    opusEncoder!!.encode(pcmInt16, 0, samplesPerFrame, encodedBuffer, 0, encodedBuffer.size)
                }
                pcmInt16.size < samplesPerFrame -> {
                    // 数据不足，填充静音
                    val paddedData = ShortArray(samplesPerFrame)
                    System.arraycopy(pcmInt16, 0, paddedData, 0, pcmInt16.size)
                    opusEncoder!!.encode(paddedData, 0, samplesPerFrame, encodedBuffer, 0, encodedBuffer.size)
                }
                else -> {
                    // 数据过长，裁剪到精确的帧长度
                    opusEncoder!!.encode(pcmInt16, 0, samplesPerFrame, encodedBuffer, 0, encodedBuffer.size)
                }
            }
            
            if (encodedBytes > 0) {
                // 返回实际编码的数据
                encodedBuffer.copyOf(encodedBytes)
            } else {
                Log.w(TAG, "Opus encoding returned 0 bytes")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Opus encoding failed", e)
            null
        }
    }
    
    /**
     * 播放Opus数据
     * 参考Flutter项目中的playOpusData方法，添加同步机制防止并发问题
     */
    suspend fun playOpusData(data: ByteArray) = withContext(Dispatchers.IO) {
        try {
            // 使用同步锁防止并发访问
            val pcmData = synchronized(audioLock) {
                // 如果播放器未初始化，先初始化
                if (opusDecoder == null) {
                    Log.w(TAG, "Opus decoder not initialized, attempting to initialize")
                    initOpusCodecs()
                    if (opusDecoder == null) {
                        Log.e(TAG, "Failed to initialize Opus decoder")
                        return@withContext
                    }
                }
                
                // 基本验证
                if (data.isEmpty()) {
                    Log.w(TAG, "Empty Opus data received")
                    return@withContext
                }
                
                Log.d(TAG, "Playing Opus data: ${data.size} bytes")
                
                // 解码Opus数据为PCM - 使用同步锁保护解码器
                synchronized(decoderLock) {
                    decodeFromOpus(data)
                }
            }
            
            if (pcmData != null) {
                Log.d(TAG, "Decoded to PCM: ${pcmData.size} bytes")
                // 播放PCM数据 - 在同步块外调用suspend函数
                playPcmData(pcmData)
            } else {
                Log.w(TAG, "Failed to decode Opus data to PCM")
            }
        } catch (e: Exception) {
            Log.e(TAG, "播放失败: $e")
            
            // 简单重置并重新初始化 - 参考Flutter的错误处理
            try {
                stopPlaying()
                synchronized(audioLock) {
                    initOpusCodecs()
                }
                Log.d(TAG, "Audio player reinitialized after error")
            } catch (reinitException: Exception) {
                Log.e(TAG, "Failed to reinitialize audio player", reinitException)
            }
        }
    }
    
    /**
     * 将Opus数据解码为PCM格式
     * 参考Flutter项目中的简洁实现，添加更好的数据验证
     */
    private fun decodeFromOpus(opusData: ByteArray): ByteArray? {
        return try {
            if (opusDecoder == null) {
                Log.w(TAG, "Opus decoder not initialized")
                return null
            }
            
            // 基本验证
            if (opusData.isEmpty()) {
                Log.w(TAG, "Empty Opus data")
                return null
            }
            
            // 对于已知有问题的960字节数据，跳过解码
            if (opusData.size == 960) {
                Log.w(TAG, "Skipping problematic 960-byte Opus data")
                return null
            }
            
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
            
            // 使用更大的缓冲区来避免数组越界
            val maxSamples = 1920
            val pcmData = ShortArray(maxSamples)
            
            // 解码Opus数据 - 参考Flutter的简洁实现
            val decodedSamples = opusDecoder!!.decode(opusData, 0, opusData.size, pcmData, 0, maxSamples, false)
            
            if (decodedSamples > 0) {
                Log.d(TAG, "Decoded $decodedSamples samples from ${opusData.size} bytes")
                
                // 转换Short数组为Byte数组（小端字节序）- 参考Flutter实现
                val byteBuffer = ByteBuffer.allocate(decodedSamples * 2).order(ByteOrder.LITTLE_ENDIAN)
                for (i in 0 until decodedSamples) {
                    byteBuffer.putShort(pcmData[i])
                }
                byteBuffer.array()
            } else {
                Log.w(TAG, "No samples decoded from Opus data")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Opus decoding failed", e)
            
            // 如果是ArrayIndexOutOfBoundsException，记录详细信息
            if (e is ArrayIndexOutOfBoundsException) {
                Log.e(TAG, "ArrayIndexOutOfBoundsException with data size: ${opusData.size} bytes")
                Log.e(TAG, "Data preview: ${opusData.take(10).joinToString(" ") { it.toString() }}")
            }
            
            null
        }
    }
    
    
    /**
     * 播放PCM数据
     * 添加同步保护，防止并发访问AudioTrack
     */
    private suspend fun playPcmData(data: ByteArray) = withContext(Dispatchers.IO) {
        synchronized(audioLock) {
            try {
                // 检查AudioTrack是否已初始化
                if (audioTrack == null) {
                    Log.w(TAG, "AudioTrack not initialized, attempting to initialize")
                    try {
                        // 使用统一的AudioTrack初始化方法
                        initAudioTrack()
                        Log.d(TAG, "AudioTrack initialized in playPcmData")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to initialize AudioTrack in playPcmData", e)
                        return@withContext
                    }
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
                
                // 不立即停止播放，让AudioTrack自然播放完成
                // 这样可以实现流式播放，减少卡顿
                
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
    
    /**
     * 停止播放
     */
    suspend fun stopPlaying() = withContext(Dispatchers.IO) {
        synchronized(audioLock) {
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
    
    /**
     * 检查并自动停止播放（当缓冲区播放完成时）
     */
    private suspend fun checkAndStopIfFinished() = withContext(Dispatchers.IO) {
        try {
            if (!isPlaying || audioTrack == null) {
                return@withContext
            }
            
            // 检查播放位置是否接近缓冲区末尾
            val bufferSize = audioTrack!!.bufferSizeInFrames
            val playbackPosition = audioTrack!!.playbackHeadPosition
            
            // 如果播放位置接近缓冲区末尾，说明播放即将完成
            if (playbackPosition >= bufferSize - 100) { // 留一些余量
                delay(50) // 等待一小段时间确保播放完成
                if (audioTrack!!.playState == AudioTrack.PLAYSTATE_STOPPED) {
                    isPlaying = false
                    Log.d(TAG, "Playback finished naturally")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check playback status", e)
        }
    }
    
    /**
     * 检查是否正在录音
     */
    fun isRecording(): Boolean = isRecording
    
    /**
     * 检查是否正在播放
     */
    fun isPlaying(): Boolean = isPlaying
    
    /**
     * 测试Opus编解码功能
     */
    fun testOpusCodec(): Boolean {
        return try {
            // 初始化编解码器
            initOpusCodecs()
            
            if (opusEncoder == null || opusDecoder == null) {
                Log.e(TAG, "Failed to initialize Opus codecs for testing")
                return false
            }
            
            // 创建测试PCM数据（1秒的静音）
            val testPcmData = ByteArray(SAMPLE_RATE * 2) // 16kHz * 2 bytes per sample
            
            // 测试编码
            val encodedData = encodeToOpus(testPcmData)
            if (encodedData == null) {
                Log.e(TAG, "Opus encoding test failed")
                return false
            }
            
            // 测试解码
            val decodedData = decodeFromOpus(encodedData)
            if (decodedData == null) {
                Log.e(TAG, "Opus decoding test failed")
                return false
            }
            
            Log.d(TAG, "Opus codec test successful - encoded: ${encodedData.size} bytes, decoded: ${decodedData.size} bytes")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Opus codec test failed with exception", e)
            false
        }
    }
    
    /**
     * 设置系统音量（而不是AudioTrack音量）
     * @param volume 音量值，范围0.0-1.0
     */
    fun setVolume(volume: Float) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val targetVolume = (volume * maxVolume).toInt().coerceIn(0, maxVolume)
            
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVolume, 0)
            Log.d(TAG, "System volume set to: $targetVolume/$maxVolume (${(volume * 100).toInt()}%)")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set system volume", e)
        }
    }
    
    /**
     * 获取当前播放音量
     */
    fun getVolume(): Float {
        return try {
            // 使用AudioManager获取系统音量
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            currentVolume.toFloat() / maxVolume.toFloat()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get volume", e)
            1.0f
        }
    }
    
    /**
     * 释放资源
     */
    suspend fun dispose() = withContext(Dispatchers.IO) {
        try {
            stopRecording()
            stopPlaying()
            
            audioRecord?.release()
            audioTrack?.release()
            
            // 释放Opus编解码器
            opusEncoder = null
            opusDecoder = null
            
            audioRecord = null
            audioTrack = null
            
            Log.d(TAG, "AudioUtil disposed")
        } catch (e: Exception) {
            Log.e(TAG, "Error disposing AudioUtil", e)
        }
    }
}
