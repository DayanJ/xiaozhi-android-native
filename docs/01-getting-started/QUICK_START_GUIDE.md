# Android AI Assistant 快速入门指南

## 🚀 5分钟快速上手

### 1. 环境准备 (2分钟)
```bash
# 确保已安装以下工具
- Android Studio (最新版本)
- JDK 8 或更高版本
- Android SDK API 24+
- Git
```

### 2. 项目导入 (1分钟)
```bash
# 使用Android Studio打开项目
1. 打开Android Studio
2. 选择 "Open an existing Android Studio project"
3. 选择 android-native-app 文件夹
4. 等待Gradle同步完成
```

### 3. 运行项目 (2分钟)
```bash
# 连接设备或启动模拟器
1. 连接Android设备并开启USB调试
2. 或启动Android模拟器
3. 点击运行按钮 (绿色三角形)
4. 等待应用安装和启动
```

## 📱 应用功能概览

### 主界面功能
- **对话列表**: 显示所有历史对话
- **创建对话**: 点击右下角"+"按钮
- **设置入口**: 点击右上角设置图标

### 对话类型
1. **Dify对话**: 基于HTTP API的文本对话
   - 支持图片上传
   - 流式响应
   - 多轮对话

2. **小智对话**: 基于WebSocket的语音对话
   - 实时语音交互
   - 语音识别
   - 语音合成

### 语音通话功能
- **按住说话**: 长按语音按钮录音
- **实时播放**: 自动播放AI回复
- **静音控制**: 可控制麦克风静音
- **音量调节**: 可调节播放音量

## 🔧 快速修改指南

### 修改UI界面
**位置**: `ui/` 目录
**示例**: 修改聊天界面
```kotlin
// 文件: ui/chat/ChatActivity.kt
// 布局: res/layout/activity_chat.xml
// 适配器: ui/chat/MessageAdapter.kt
```

### 修改业务逻辑
**位置**: `service/` 目录
**示例**: 修改小智服务
```kotlin
// 文件: service/XiaozhiService.kt
// WebSocket: service/XiaozhiWebSocketManager.kt
// 音频: service/AudioUtil.kt
```

### 修改数据模型
**位置**: `model/` 目录
**示例**: 添加新字段
```kotlin
// 1. 修改 model/Message.kt
// 2. 修改 database/entity/MessageEntity.kt
// 3. 修改 database/dao/MessageDao.kt
// 4. 更新数据库版本
```

## 🐛 常见问题解决

### 1. 编译失败
```bash
# 检查Gradle版本
./gradlew --version

# 清理项目
./gradlew clean

# 重新构建
./gradlew build
```

### 2. 网络连接失败
```bash
# 检查网络权限
# 文件: AndroidManifest.xml
<uses-permission android:name="android.permission.INTERNET" />

# 检查配置
# 文件: ui/settings/SettingsActivity.kt
```

### 3. 音频功能异常
```bash
# 检查音频权限
# 文件: AndroidManifest.xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />

# 检查音频服务
# 文件: service/AudioUtil.kt
```

### 4. 数据库错误
```bash
# 检查数据库版本
# 文件: database/AppDatabase.kt

# 清理应用数据
# 设置 -> 应用 -> AI Assistant -> 存储 -> 清除数据
```

## 📝 开发工作流

### 1. 添加新功能
```bash
1. 在 model/ 中定义数据模型
2. 在 database/ 中添加数据库支持
3. 在 service/ 中实现业务逻辑
4. 在 viewmodel/ 中添加状态管理
5. 在 ui/ 中创建界面
6. 在 AndroidManifest.xml 中注册Activity
```

### 2. 修改现有功能
```bash
1. 定位相关文件
2. 理解现有逻辑
3. 进行修改
4. 测试功能
5. 提交代码
```

### 3. 调试技巧
```bash
# 使用Log输出调试信息
Log.d("TAG", "Debug message")

# 使用断点调试
# 在Android Studio中设置断点

# 查看logcat输出
# View -> Tool Windows -> Logcat
```

## 🎯 核心文件说明

### 主要Activity
- **MainActivity.kt**: 应用入口，对话列表
- **ChatActivity.kt**: 聊天界面，消息发送接收
- **VoiceCallActivity.kt**: 语音通话界面
- **SettingsActivity.kt**: 设置界面

### 核心服务
- **XiaozhiService.kt**: 小智WebSocket服务
- **DifyService.kt**: Dify HTTP服务
- **AudioUtil.kt**: 音频处理工具

### 数据管理
- **ConversationViewModel.kt**: 对话状态管理
- **ConfigViewModel.kt**: 配置状态管理
- **AppDatabase.kt**: 数据库主类

## 🔍 代码搜索技巧

### 快速定位功能
```bash
# 搜索关键词
Ctrl+Shift+F (全局搜索)
Ctrl+F (文件内搜索)

# 常用搜索词
- "onCreate" - 查找Activity初始化
- "sendMessage" - 查找消息发送逻辑
- "WebSocket" - 查找网络连接
- "AudioTrack" - 查找音频播放
- "LiveData" - 查找状态管理
```

### 理解代码结构
```bash
# 查看类继承关系
Ctrl+H (Type Hierarchy)

# 查看方法调用关系
Ctrl+Alt+H (Call Hierarchy)

# 查看文件结构
Ctrl+F12 (File Structure)
```

## 📚 学习资源

### 官方文档
- [Android开发者指南](https://developer.android.com/guide)
- [Kotlin官方文档](https://kotlinlang.org/docs/)
- [Room数据库指南](https://developer.android.com/training/data-storage/room)

### 项目相关文档
- [项目分析指南](PROJECT_ANALYSIS_GUIDE.md)
- [架构图说明](ARCHITECTURE_DIAGRAMS.md)
- [编译指南](COMPILE_GUIDE.md)

## 🎉 下一步

1. **熟悉项目结构**: 阅读 [PROJECT_ANALYSIS_GUIDE.md](PROJECT_ANALYSIS_GUIDE.md)
2. **理解架构设计**: 查看 [ARCHITECTURE_DIAGRAMS.md](ARCHITECTURE_DIAGRAMS.md)
3. **开始开发**: 根据需求修改相应文件
4. **测试功能**: 在真机上测试所有功能
5. **提交代码**: 使用Git管理代码版本

---

**提示**: 这是一个功能完整的Android AI助手应用，代码结构清晰，注释详细。新手可以通过本文档快速上手，并根据具体需求进行修改和扩展。
