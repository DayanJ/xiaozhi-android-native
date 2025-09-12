# AI Assistant Android Native App

这是将Flutter AI助手应用完全翻译成Android原生应用的项目。

## 功能特性

### 1. 对话类型
- **Dify对话**: 基于HTTP API的文本对话，支持图片上传
- **小智对话**: 基于WebSocket的语音对话，支持实时语音交互

### 2. 主要功能
- 对话管理（创建、删除、置顶）
- 消息发送和接收
- 语音通话（仅小智对话）
- 配置管理（Dify和小智配置）
- 主题设置

## 项目结构

```
app/src/main/java/com/lhht/aiassistant/
├── MainActivity.kt                    # 主Activity
├── model/                            # 数据模型
│   ├── Conversation.kt               # 对话模型
│   ├── Message.kt                    # 消息模型
│   ├── DifyConfig.kt                 # Dify配置模型
│   ├── XiaozhiConfig.kt              # 小智配置模型
│   ├── ConversationType.kt           # 对话类型枚举
│   └── MessageRole.kt                # 消息角色枚举
├── service/                          # 服务层
│   ├── DifyService.kt                # Dify服务
│   ├── XiaozhiService.kt             # 小智服务
│   ├── XiaozhiWebSocketManager.kt    # WebSocket管理器
│   ├── AudioUtil.kt                  # 音频工具
│   ├── XiaozhiServiceEvent.kt        # 服务事件
│   └── Completer.kt                  # 异步完成器
├── viewmodel/                        # 状态管理
│   ├── ConversationViewModel.kt      # 对话ViewModel
│   ├── ConfigViewModel.kt            # 配置ViewModel
│   └── ThemeViewModel.kt             # 主题ViewModel
├── ui/                               # UI界面
│   ├── main/                         # 主界面
│   │   └── ConversationAdapter.kt    # 对话适配器
│   ├── chat/                         # 聊天界面
│   │   ├── ChatActivity.kt           # 聊天Activity
│   │   └── MessageAdapter.kt         # 消息适配器
│   ├── conversation/                 # 对话类型选择
│   │   └── ConversationTypeActivity.kt
│   ├── settings/                     # 设置界面
│   │   └── SettingsActivity.kt
│   ├── config/                       # 配置管理
│   │   ├── ConfigSelectorActivity.kt
│   │   ├── ConfigEditActivity.kt
│   │   └── ConfigAdapter.kt
│   └── voice/                        # 语音通话
│       └── VoiceCallActivity.kt
└── utils/                            # 工具类
    └── DeviceUtil.kt                 # 设备工具
```

## 技术栈

- **语言**: Kotlin
- **UI框架**: Android Views + ViewBinding
- **架构**: MVVM + Repository Pattern
- **网络**: OkHttp + Retrofit
- **WebSocket**: OkHttp WebSocket
- **音频**: Android MediaRecorder/AudioTrack
- **状态管理**: LiveData + ViewModel
- **依赖注入**: 手动依赖注入

## 依赖库

```gradle
// 核心Android库
implementation 'androidx.core:core-ktx:1.12.0'
implementation 'androidx.appcompat:appcompat:1.6.1'
implementation 'com.google.android.material:material:1.10.0'
implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
implementation 'androidx.lifecycle:lifecycle-livedata-ktx:2.7.0'
implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0'
implementation 'androidx.navigation:navigation-fragment-ktx:2.7.5'
implementation 'androidx.navigation:navigation-ui-ktx:2.7.5'
implementation 'androidx.recyclerview:recyclerview:1.3.2'
implementation 'androidx.cardview:cardview:1.0.0'

// 网络库
implementation 'com.squareup.okhttp3:okhttp:4.12.0'
implementation 'com.squareup.retrofit2:retrofit:2.9.0'
implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
implementation 'com.squareup.okhttp3:logging-interceptor:4.12.0'

// 其他工具库
implementation 'com.github.bumptech.glide:glide:4.16.0'
implementation 'com.google.code.gson:gson:2.10.1'
implementation 'com.karumi:dexter:6.2.3'
implementation 'androidx.media:media:1.7.0'
implementation 'androidx.preference:preference-ktx:1.2.1'
```

## 编译和运行

### 环境要求
- Android Studio Arctic Fox 或更高版本
- JDK 8 或更高版本
- Android SDK API 24 或更高版本
- Gradle 8.4

### 编译步骤

1. 打开Android Studio
2. 导入项目
3. 等待Gradle同步完成
4. 连接Android设备或启动模拟器
5. 点击运行按钮

### 命令行编译

```bash
# 编译Debug版本
./gradlew assembleDebug

# 编译Release版本
./gradlew assembleRelease

# 安装到设备
./gradlew installDebug
```

## 配置说明

### Dify配置
- API URL: Dify服务的API地址
- API Key: Dify服务的API密钥

### 小智配置
- WebSocket URL: 小智WebSocket服务地址
- MAC地址: 设备MAC地址
- Token: 认证令牌

## 权限说明

应用需要以下权限：
- `INTERNET`: 网络访问
- `ACCESS_NETWORK_STATE`: 网络状态检查
- `RECORD_AUDIO`: 录音权限（语音功能）
- `MODIFY_AUDIO_SETTINGS`: 音频设置修改
- `READ_EXTERNAL_STORAGE`: 读取外部存储（图片上传）
- `WRITE_EXTERNAL_STORAGE`: 写入外部存储
- `CAMERA`: 相机权限（拍照上传）
- `BLUETOOTH`: 蓝牙权限
- `BLUETOOTH_CONNECT`: 蓝牙连接权限
- `WAKE_LOCK`: 唤醒锁权限

## 注意事项

1. 首次运行需要配置Dify或小智服务
2. 语音功能需要麦克风权限
3. 图片上传功能需要存储权限
4. 网络连接是必需的

## 与原Flutter应用的对比

| 功能 | Flutter版本 | Android原生版本 |
|------|-------------|-----------------|
| 对话管理 | ✅ | ✅ |
| Dify对话 | ✅ | ✅ |
| 小智对话 | ✅ | ✅ |
| 语音通话 | ✅ | ✅ |
| 图片上传 | ✅ | ✅ |
| 配置管理 | ✅ | ✅ |
| 主题设置 | ✅ | ✅ |
| 状态管理 | Provider | ViewModel + LiveData |
| 网络请求 | HTTP | OkHttp + Retrofit |
| WebSocket | WebSocket | OkHttp WebSocket |
| 音频处理 | Flutter Audio | Android MediaRecorder |

## 开发说明

这个Android原生应用完全复现了原Flutter应用的所有功能，包括：
- 相同的用户界面和交互体验
- 相同的业务逻辑和数据处理
- 相同的网络通信和WebSocket连接
- 相同的音频录制和播放功能
- 相同的配置管理和数据持久化

所有代码都使用Kotlin编写，遵循Android开发最佳实践，具有良好的可维护性和扩展性。
