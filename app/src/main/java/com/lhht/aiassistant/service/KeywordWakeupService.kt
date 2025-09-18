package com.lhht.aiassistant.service

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import com.k2fsa.sherpa.onnx.KeywordSpotter
import com.k2fsa.sherpa.onnx.KeywordSpotterConfig
import com.k2fsa.sherpa.onnx.FeatureConfig
import com.k2fsa.sherpa.onnx.OnlineStream
import com.k2fsa.sherpa.onnx.getKwsModelConfig
import com.k2fsa.sherpa.onnx.getKeywordsFile
import kotlinx.coroutines.*
import kotlin.concurrent.thread

/**
 * 关键词唤醒服务
 * 基于SherpaOnnx实现语音唤醒功能
 */
class KeywordWakeupService(private val context: Context) {
    
    companion object {
        private const val TAG = "KeywordWakeupService"
        private const val SAMPLE_RATE = 16000
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private const val BUFFER_INTERVAL_MS = 100L // 100ms缓冲区
    }
    
    private var kws: KeywordSpotter? = null
    private var stream: OnlineStream? = null
    private var audioRecord: AudioRecord? = null
    private var recordingThread: Thread? = null
    
    @Volatile
    private var isListening = false
    
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // 唤醒回调
    private var wakeupCallback: ((String) -> Unit)? = null
    
    // 默认关键词（中文）
    private val defaultKeywords = "x iǎo ān x iǎo ān"
    
    /**
     * 初始化关键词识别器
     */
    suspend fun initialize() = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Initializing keyword spotter...")
            
            // 使用中文模型 (type = 0)
            val config = KeywordSpotterConfig(
                featConfig = FeatureConfig(
                    sampleRate = SAMPLE_RATE,
                    featureDim = 80
                ),
                modelConfig = getKwsModelConfig(type = 0) ?: throw IllegalStateException("Failed to get model config"),
                keywordsFile = getKeywordsFile(type = 0),
                keywordsThreshold = 0.25f, // 降低阈值提高敏感度
                keywordsScore = 1.5f
            )
            
            kws = KeywordSpotter(
                assetManager = context.assets,
                config = config
            )
            
            stream = kws?.createStream(defaultKeywords)
            
            if (stream?.ptr == 0L) {
                throw IllegalStateException("Failed to create keyword stream")
            }
            
            Log.d(TAG, "Keyword spotter initialized successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize keyword spotter", e)
            throw e
        }
    }
    
    /**
     * 开始监听关键词
     */
    fun startListening(callback: (String) -> Unit) {
        if (isListening) {
            Log.w(TAG, "Already listening")
            return
        }
        
        wakeupCallback = callback
        
        try {
            if (!initializeMicrophone()) {
                Log.e(TAG, "Failed to initialize microphone")
                return
            }
            
            audioRecord?.startRecording()
            isListening = true
            
            recordingThread = thread(true) {
                processAudioSamples()
            }
            
            Log.d(TAG, "Started keyword listening")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start listening", e)
            stopListening()
        }
    }
    
    /**
     * 停止监听关键词
     */
    fun stopListening() {
        if (!isListening) {
            return
        }
        
        isListening = false
        
        try {
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
            
            recordingThread?.interrupt()
            recordingThread = null
            
            stream?.release()
            
            Log.d(TAG, "Stopped keyword listening")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping listening", e)
        }
    }
    
    /**
     * 设置自定义关键词
     */
    fun setKeywords(keywords: String) {
        try {
            stream?.release()
            stream = kws?.createStream(keywords)
            
            if (stream?.ptr == 0L) {
                Log.e(TAG, "Failed to set keywords: $keywords")
            } else {
                Log.d(TAG, "Keywords set to: $keywords")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set keywords", e)
        }
    }
    
    /**
     * 释放资源
     */
    fun release() {
        stopListening()
        
        try {
            kws?.release()
            kws = null
            stream = null
            
            scope.cancel()
            
            Log.d(TAG, "Keyword wakeup service released")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing service", e)
        }
    }
    
    /**
     * 初始化麦克风
     */
    private fun initializeMicrophone(): Boolean {
        try {
            // 检查录音权限
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                val permission = android.Manifest.permission.RECORD_AUDIO
                if (context.checkSelfPermission(permission) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, "RECORD_AUDIO permission not granted")
                    return false
                }
            }
            
            val bufferSize = AudioRecord.getMinBufferSize(
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT
            )
            
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                bufferSize * 2
            )
            
            return audioRecord?.state == AudioRecord.STATE_INITIALIZED
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize microphone", e)
            return false
        }
    }
    
    /**
     * 处理音频样本
     */
    private fun processAudioSamples() {
        Log.d(TAG, "Processing audio samples")
        
        val bufferSize = (BUFFER_INTERVAL_MS * SAMPLE_RATE / 1000).toInt()
        val buffer = ShortArray(bufferSize)
        
        while (isListening && !Thread.currentThread().isInterrupted) {
            try {
                val ret = audioRecord?.read(buffer, 0, buffer.size)
                
                if (ret != null && ret > 0) {
                    // 转换为浮点数组
                    val samples = FloatArray(ret) { buffer[it] / 32768.0f }
                    
                    // 处理音频样本
                    stream?.let { s ->
                        s.acceptWaveform(samples, SAMPLE_RATE)
                        
                        // 检查是否有识别结果
                        while (kws?.isReady(s) == true) {
                            kws?.decode(s)
                            
                            val result = kws?.getResult(s)
                            val keyword = result?.keyword
                            
                            if (!keyword.isNullOrBlank()) {
                                Log.d(TAG, "Keyword detected: $keyword")
                                
                                // 重置流
                                kws?.reset(s)
                                
                                // 回调通知
                                wakeupCallback?.invoke(keyword)
                            }
                        }
                    }
                }
                
            } catch (e: Exception) {
                if (isListening) {
                    Log.e(TAG, "Error processing audio samples", e)
                }
            }
        }
    }
    
    /**
     * 检查是否正在监听
     */
    fun isListening(): Boolean = isListening
    
    /**
     * 检查服务是否已初始化
     */
    fun isInitialized(): Boolean = kws != null && stream != null
}
