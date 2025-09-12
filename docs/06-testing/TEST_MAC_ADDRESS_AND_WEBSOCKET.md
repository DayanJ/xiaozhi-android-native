# MAC地址自动获取和小智WebSocket协议测试指南

## 已实现的功能

### 1. 自动获取设备MAC地址
- ✅ 创建了`DeviceUtil`工具类
- ✅ 支持多种MAC地址获取方式：
  - WiFi MAC地址（Android 10以下）
  - 网络接口MAC地址
  - 基于设备信息生成固定ID
  - 随机UUID回退方案
- ✅ 在小智配置添加时自动填充MAC地址

### 2. 小智WebSocket协议实现
- ✅ 更新了`XiaozhiWebSocketManager`，参考Flutter工程协议
- ✅ 实现了完整的连接握手流程：
  - 添加必要的HTTP headers（device-id, client-id, protocol-version, Authorization）
  - 发送Hello消息（包含音频参数）
  - 处理各种消息类型（tts, stt, emotion, listen, error）
- ✅ 实现了语音交互控制：
  - 开始/停止语音监听
  - 发送文本请求
  - 处理音频数据

## 测试步骤

### 测试用例1：MAC地址自动获取

#### 步骤1：测试小智配置添加
1. **进入设置页面**
   - 点击主页面右上角的设置按钮
   - 点击"小智配置"

2. **添加新配置**
   - 点击右下角的"+"按钮
   - 观察MAC地址字段是否自动填充

3. **验证MAC地址格式**
   - MAC地址应该是12位十六进制字符（无冒号分隔）
   - 例如：`AABBCCDDEEFF` 或 `123456789ABC`

#### 预期结果：
- MAC地址字段自动填充，无需手动输入
- MAC地址格式正确（12位十六进制字符）
- 日志中显示：`ConfigEdit: 自动获取MAC地址: [MAC地址]`

### 测试用例2：小智WebSocket连接协议

#### 步骤1：配置小智服务
1. **填写完整配置**
   - 配置名称：`测试小智配置`
   - WebSocket地址：`ws://your-server:port` 或 `wss://your-server:port`
   - MAC地址：自动填充（已验证）
   - Token：`your-token`

2. **保存配置**
   - 点击保存按钮
   - 返回配置列表，确认配置已保存

#### 步骤2：测试WebSocket连接
1. **启动小智对话**
   - 从主页面选择小智对话模式
   - 系统应该使用保存的配置建立连接

2. **观察连接日志**
   - 查看日志中的连接过程：
     ```
     XiaozhiWebSocketManager: Connecting to WebSocket: [URL]
     XiaozhiWebSocketManager: Device ID: [MAC地址]
     XiaozhiWebSocketManager: Token enabled: true
     XiaozhiWebSocketManager: Added Authorization header: Bearer [token]
     XiaozhiWebSocketManager: WebSocket connected
     XiaozhiWebSocketManager: Sending hello message: {...}
     ```

3. **验证Hello消息格式**
   - Hello消息应包含：
     ```json
     {
       "type": "hello",
       "version": 1,
       "transport": "websocket",
       "audio_params": {
         "format": "opus",
         "sample_rate": 16000,
         "channels": 1,
         "frame_duration": 60
       }
     }
     ```

#### 步骤3：测试消息交互
1. **发送文本消息**
   - 在对话界面输入文本
   - 观察发送的消息格式：
     ```json
     {
       "type": "listen",
       "state": "detect",
       "text": "用户输入的文本",
       "source": "text"
     }
     ```

2. **测试语音监听**
   - 点击语音按钮开始录音
   - 观察发送的监听控制消息：
     ```json
     {
       "type": "listen",
       "state": "start"
     }
     ```

3. **验证消息处理**
   - 观察接收到的各种消息类型：
     - `tts`：文本转语音消息
     - `stt`：语音转文本结果
     - `emotion`：表情信息
     - `listen`：监听状态变化
     - `error`：错误信息

## 验证要点

### MAC地址获取验证：
1. **自动填充**：添加小智配置时MAC地址字段自动填充
2. **格式正确**：MAC地址为12位十六进制字符
3. **唯一性**：同一设备生成的MAC地址应该一致
4. **兼容性**：在不同Android版本上都能正常工作

### WebSocket协议验证：
1. **连接建立**：能够成功建立WebSocket连接
2. **认证正确**：发送正确的Authorization header
3. **Hello握手**：发送符合协议的Hello消息
4. **消息格式**：发送和接收的消息格式符合协议规范
5. **状态管理**：正确处理连接状态变化

## 故障排除

### MAC地址获取问题：
- **如果MAC地址为空**：检查设备权限，查看日志中的错误信息
- **如果格式不正确**：检查`DeviceUtil.getMacAddress()`方法的实现
- **如果无法获取**：系统会使用设备信息生成固定ID作为回退方案

### WebSocket连接问题：
- **连接失败**：检查WebSocket URL格式和网络连接
- **认证失败**：检查Token是否正确，查看Authorization header
- **协议错误**：检查Hello消息格式是否符合服务器要求
- **消息处理错误**：查看日志中的消息解析错误

## 相关文件

- `DeviceUtil.kt`：设备工具类，负责MAC地址获取
- `ConfigEditActivity.kt`：配置编辑页面，自动填充MAC地址
- `XiaozhiWebSocketManager.kt`：WebSocket管理器，实现连接协议
- `XiaozhiService.kt`：小智服务，使用配置建立连接

## 测试完成标准

✅ MAC地址自动获取功能正常  
✅ 小智配置添加时MAC地址自动填充  
✅ WebSocket连接协议实现正确  
✅ Hello消息格式符合规范  
✅ 消息发送和接收格式正确  
✅ 错误处理和日志记录完善  

测试完成后，小智服务应该能够：
1. 自动获取设备MAC地址并用于配置
2. 使用配置信息建立符合协议的WebSocket连接
3. 正确处理各种消息类型和状态变化
4. 提供完整的语音交互功能
