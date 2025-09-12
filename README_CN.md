# xiaozhi-android-native

[![English](https://img.shields.io/badge/English-blue)](README.md)
[![中文](https://img.shields.io/badge/中文-red)](README_CN.md)

> 一个原生Android AI助手应用，具有语音交互功能，支持Dify对话和小智语音聊天。

**Language / 语言**: [English](README.md) | [中文](README_CN.md)

## 🚀 功能特性

- **双模式对话**: Dify HTTP对话 + 小智WebSocket语音对话
- **实时语音通信**: 语音录制、播放和实时交互
- **数据持久化**: 对话历史和配置信息的本地存储
- **配置管理**: 灵活的Dify和小智配置管理
- **语音唤醒**: 使用SherpaOnnx的本地关键词识别
- **主题支持**: 可定制的界面主题

## 📱 截图

*截图将在此处添加*

## 🛠️ 技术栈

- **架构**: MVVM + Repository模式
- **语言**: Kotlin 100%
- **UI框架**: Android Views + ViewBinding
- **数据库**: Room (SQLite)
- **网络**: OkHttp + Retrofit + WebSocket
- **音频**: MediaRecorder + AudioTrack + Opus编解码
- **语音识别**: SherpaOnnx (本地关键词识别)

## 📋 系统要求

- Android Studio (最新版本)
- JDK 8 或更高版本
- Android SDK API 24+
- 带麦克风和扬声器的Android设备

## 🚀 快速开始

### 1. 克隆仓库
```bash
git clone https://github.com/DayanJ/xiaozhi-android-native.git
cd xiaozhi-android-native
```

### 2. 在Android Studio中打开
1. 打开Android Studio
2. 选择"打开现有项目"
3. 导航到克隆的目录
4. 等待Gradle同步完成

### 3. 配置项目
1. 在应用中更新服务器配置
2. 配置Dify API端点
3. 设置小智WebSocket连接

### 4. 运行应用
1. 连接Android设备或启动模拟器
2. 点击Android Studio中的运行按钮
3. 授予必要权限（麦克风、存储）

## 📁 项目结构

```
app/src/main/java/com/lhht/aiassistant/
├── MainActivity.kt                    # 主入口
├── model/                            # 数据模型
├── database/                         # 数据库层
├── repository/                       # 数据仓库
├── service/                          # 业务服务
├── viewmodel/                        # 状态管理
├── ui/                               # 用户界面
└── utils/                            # 工具类
```

## 🔧 配置

### Dify配置
- 设置您的Dify API端点
- 配置API密钥和身份验证
- 设置对话参数

### 小智配置
- 配置WebSocket服务器URL
- 设置设备标识
- 配置音频参数

## 🎯 核心功能

### 语音交互
- 实时语音录制和播放
- Opus音频编解码，高效传输
- 语音活动检测
- 音频预处理和优化

### 对话管理
- 持久化对话历史
- 多对话支持
- 消息线程和组织
- 导出和导入功能

### 语音唤醒
- 使用SherpaOnnx的本地关键词识别
- 可自定义唤醒词
- 低延迟语音激活
- 注重隐私的本地处理

## 📚 文档

详细文档可在 [docs/](docs/) 目录中找到：

- 🚀 **[快速开始](docs/01-getting-started/)** - 新用户指南
- 🏗️ **[架构设计](docs/02-architecture/)** - 项目架构和设计
- 💻 **[开发指南](docs/03-development/)** - 开发指南
- 🐛 **[问题修复](docs/04-bug-fixes/)** - 问题修复和解决方案
- ⚡ **[性能优化](docs/05-optimization/)** - 性能优化
- 🧪 **[测试相关](docs/06-testing/)** - 测试和调试
- 📊 **[报告分析](docs/07-reports/)** - 分析报告

## 🔄 开发

### 构建项目
```bash
./gradlew assembleDebug
```

### 运行测试
```bash
./gradlew test
```

### 代码风格
项目遵循Android Kotlin风格指南。使用`ktlint`进行代码格式化。

## 🤝 贡献

我们欢迎贡献！请按照以下步骤：

1. Fork 仓库
2. 创建功能分支 (`git checkout -b feature/amazing-feature`)
3. 提交更改 (`git commit -m 'Add some amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 创建 Pull Request

### 贡献指南
- 遵循现有代码风格
- 为新功能添加测试
- 根据需要更新文档
- 确保所有测试通过

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 🙏 致谢

- [SherpaOnnx](https://github.com/k2-fsa/sherpa-onnx) 提供语音识别功能
- [Concentus](https://github.com/lostromb/concentus) 提供Opus音频编解码
- [OkHttp](https://square.github.io/okhttp/) 提供网络通信
- [Room](https://developer.android.com/training/data-storage/room) 提供本地数据库

## 📞 支持

如果您遇到任何问题或有疑问：

- 创建 [Issue](https://github.com/DayanJ/xiaozhi-android-native/issues)
- 邮箱: jingdayanw@gmail.com
- 查看 [文档](docs/)
- 查看现有 [问题修复](docs/04-bug-fixes/)

## 🔗 相关项目

本项目是小智AI助手生态系统的一部分。相关项目包括：
- [xiaozhi-esp32](https://github.com/DayanJ/xiaozhi-esp32) - ESP32硬件实现
- [xiaozhi-esp32-server-java](https://github.com/DayanJ/xiaozhi-esp32-server-java) - 后端服务
- [xiaozhi-android-client](https://github.com/DayanJ/xiaozhi-android-client) - Flutter版本

---

**注意**: 这是小智AI助手的原生Android实现，为语音交互功能提供最佳性能和用户体验。
