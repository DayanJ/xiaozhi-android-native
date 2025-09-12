package com.lhht.aiassistant.service

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * 类似于Dart中的Completer，用于异步操作的结果处理
 */
class Completer<T> {
    private var _isCompleted = false
    private var _result: T? = null
    private var _exception: Throwable? = null
    
    val isCompleted: Boolean get() = _isCompleted
    
    /**
     * 完成操作并返回结果
     */
    fun complete(result: T) {
        if (_isCompleted) return
        _isCompleted = true
        _result = result
    }
    
    /**
     * 完成操作并抛出异常
     */
    fun completeError(exception: Throwable) {
        if (_isCompleted) return
        _isCompleted = true
        _exception = exception
    }
    
    /**
     * 等待结果
     */
    suspend fun await(): T = suspendCancellableCoroutine { continuation ->
        if (_isCompleted) {
            if (_exception != null) {
                continuation.resumeWithException(_exception!!)
            } else {
                continuation.resume(_result!!)
            }
        } else {
            // 这里需要实现等待逻辑，但为了简化，我们使用一个简单的轮询
            // 在实际项目中，应该使用更合适的同步机制
            Thread {
                while (!_isCompleted) {
                    Thread.sleep(10)
                }
                if (_exception != null) {
                    continuation.resumeWithException(_exception!!)
                } else {
                    continuation.resume(_result!!)
                }
            }.start()
        }
    }
}
