# GitHub仓库About描述

## 简短描述
小智是一个智能语音助手应用，支持多种AI服务： • Dify对话：基于HTTP API的文本对话，支持图片上传 • 小智对话：基于WebSocket的语音对话，支持实时语音交互 • 语音唤醒：本地关键词识别，支持自定义唤醒词

## 详细描述

### 项目简介
xiaozhi-android-native 是一个功能完整的Android原生AI助手应用，集成了多种AI服务，提供流畅的语音交互体验。作为小智AI助手生态系统的原生Android实现，该项目展示了现代Android开发的最佳实践。

### 核心功能
- **双模式对话系统**: 支持Dify HTTP API文本对话和小智WebSocket语音对话
- **实时语音通信**: 基于Opus编解码的高质量音频传输
- **本地语音唤醒**: 使用SherpaOnnx实现的关键词识别，支持"小安小安"等自定义唤醒词
- **数据持久化**: 基于Room数据库的对话历史和配置管理
- **现代架构**: MVVM + Repository模式，100% Kotlin开发

### 技术亮点
- **音频处理**: 集成Opus编解码器，支持实时音频流处理
- **语音识别**: 本地关键词识别，保护用户隐私
- **网络通信**: WebSocket长连接，支持实时双向通信
- **UI设计**: Material Design，支持深色/浅色主题切换
- **性能优化**: 16KB页面大小兼容，支持未来Android设备

### 开源特性
- **完整文档**: 64个文档文件，涵盖架构、开发、测试等各个方面
- **代码质量**: 遵循Android开发最佳实践，完整的错误处理
- **社区友好**: 详细的贡献指南，欢迎社区参与
- **MIT许可证**: 开源友好，支持商业使用

### 适用场景
- AI助手应用开发参考
- 语音交互技术学习
- Android原生开发实践
- 开源项目贡献参与

### 相关项目
- [xiaozhi-esp32](https://github.com/DayanJ/xiaozhi-esp32) - ESP32硬件实现
- [xiaozhi-esp32-server-java](https://github.com/DayanJ/xiaozhi-esp32-server-java) - 后端服务
- [xiaozhi-android-client](https://github.com/DayanJ/xiaozhi-android-client) - Flutter版本

## 标签建议
`android` `kotlin` `ai-assistant` `voice-recognition` `websocket` `opus` `sherpa-onnx` `mvvm` `room-database` `material-design` `voice-wake-up` `real-time-communication` `open-source`

## 网站链接
- 项目文档: [docs/](docs/)
- 问题反馈: [Issues](https://github.com/DayanJ/xiaozhi-android-native/issues)
- 贡献指南: [CONTRIBUTING.md](CONTRIBUTING.md)

## 联系方式
- 邮箱: jingdayanw@gmail.com
- 项目维护者: DayanJ

---

**注意**: 这是小智AI助手的原生Android实现，为语音交互功能提供最佳性能和用户体验。项目采用现代Android开发技术栈，适合学习、参考和二次开发。
