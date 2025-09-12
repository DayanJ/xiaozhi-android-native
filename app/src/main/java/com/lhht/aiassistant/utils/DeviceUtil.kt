package com.lhht.aiassistant.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import java.net.NetworkInterface
import java.util.*

/**
 * 设备工具类
 * 参考Flutter工程中的DeviceUtil实现
 */
object DeviceUtil {
    private const val TAG = "DeviceUtil"
    private const val DEVICE_ID_KEY = "device_id"

    /**
     * 获取设备MAC地址
     * 优先获取WiFi MAC地址，如果无法获取则生成一个固定的设备ID
     */
    fun getMacAddress(context: Context): String {
        return try {
            // 方法1: 尝试获取WiFi MAC地址
            val wifiMacAddress = getWifiMacAddress(context)
            if (wifiMacAddress.isNotEmpty()) {
                Log.d(TAG, "获取到WiFi MAC地址: $wifiMacAddress")
                return wifiMacAddress
            }

            // 方法2: 尝试从网络接口获取MAC地址
            val networkMacAddress = getNetworkMacAddress()
            if (networkMacAddress.isNotEmpty()) {
                Log.d(TAG, "获取到网络接口MAC地址: $networkMacAddress")
                return networkMacAddress
            }

            // 方法3: 如果无法获取真实MAC地址，生成一个基于设备信息的固定ID
            val deviceId = generateDeviceId(context)
            Log.d(TAG, "生成设备ID: $deviceId")
            deviceId

        } catch (e: Exception) {
            Log.e(TAG, "获取MAC地址失败", e)
            // 最后回退方案：生成一个随机UUID
            val fallbackId = UUID.randomUUID().toString().replace("-", "").substring(0, 12)
            Log.d(TAG, "使用回退方案生成ID: $fallbackId")
            fallbackId
        }
    }

    /**
     * 获取WiFi MAC地址
     */
    private fun getWifiMacAddress(context: Context): String {
        return try {
            // Android 6.0以上需要位置权限才能获取WiFi MAC地址
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Log.w(TAG, "没有位置权限，无法获取WiFi MAC地址")
                    return ""
                }
            }

            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10及以上版本，无法获取真实MAC地址
                null
            } else {
                wifiManager.connectionInfo
            }
            val macAddress = wifiInfo?.macAddress

            if (macAddress != null && macAddress != "02:00:00:00:00:00") {
                // 移除冒号并转换为大写
                macAddress.replace(":", "").uppercase()
            } else {
                ""
            }
        } catch (e: Exception) {
            Log.e(TAG, "获取WiFi MAC地址失败", e)
            ""
        }
    }

    /**
     * 从网络接口获取MAC地址
     */
    private fun getNetworkMacAddress(): String {
        return try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            for (networkInterface in Collections.list(interfaces)) {
                val macBytes = networkInterface.hardwareAddress
                if (macBytes != null && macBytes.size == 6) {
                    val macAddress = StringBuilder()
                    for (i in macBytes.indices) {
                        macAddress.append(String.format("%02X", macBytes[i]))
                        if (i < macBytes.size - 1) {
                            macAddress.append(":")
                        }
                    }
                    val mac = macAddress.toString()
                    // 过滤掉一些无效的MAC地址
                    if (mac != "00:00:00:00:00:00" && !mac.startsWith("02:00:00")) {
                        return mac.replace(":", "").uppercase()
                    }
                }
            }
            ""
        } catch (e: Exception) {
            Log.e(TAG, "从网络接口获取MAC地址失败", e)
            ""
        }
    }

    /**
     * 生成基于设备信息的设备ID
     * 参考Flutter工程中的实现
     */
    private fun generateDeviceId(context: Context): String {
        return try {
            val manufacturer = Build.MANUFACTURER
            val model = Build.MODEL
            val deviceName = "${manufacturer}_${model}"
            
            // 格式化设备名称，移除特殊字符
            val formattedName = formatDeviceName(deviceName)
            
            // 如果格式化后为空，使用Android ID
            if (formattedName.isEmpty()) {
                val androidId = Settings.Secure.getString(
                    context.contentResolver,
                    Settings.Secure.ANDROID_ID
                )
                if (androidId != null && androidId.isNotEmpty()) {
                    return androidId.uppercase()
                }
            }
            
            formattedName
        } catch (e: Exception) {
            Log.e(TAG, "生成设备ID失败", e)
            ""
        }
    }

    /**
     * 格式化设备名称，移除特殊字符并规范化格式
     * 参考Flutter工程中的_formatDeviceName方法
     */
    private fun formatDeviceName(name: String): String {
        if (name.isEmpty()) return "unknown_device"

        // 1. 转换为小写
        var formatted = name.lowercase()

        // 2. 替换空格和特殊字符为下划线
        formatted = formatted.replace(Regex("[^a-z0-9]"), "_")

        // 3. 替换连续的下划线为单个下划线
        formatted = formatted.replace(Regex("_+"), "_")

        // 4. 移除开头和结尾的下划线
        formatted = formatted.replace(Regex("^_+|_+$"), "")

        // 5. 如果处理后为空，返回默认值
        if (formatted.isEmpty()) return "unknown_device"

        // 6. 限制长度
        if (formatted.length > 32) {
            formatted = formatted.substring(0, 32)
            // 确保不以下划线结尾
            formatted = formatted.replace(Regex("_+$"), "")
        }

        return formatted
    }

    /**
     * 获取设备型号
     */
    fun getDeviceModel(): String {
        return "${Build.MANUFACTURER} ${Build.MODEL}"
    }

    /**
     * 获取操作系统版本
     */
    fun getOsVersion(): String {
        return "Android ${Build.VERSION.RELEASE}"
    }

    /**
     * 生成唯一的会话ID
     */
    fun generateConversationId(): String {
        return UUID.randomUUID().toString()
    }
}