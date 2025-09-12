# 变更日志

[![English](https://img.shields.io/badge/English-blue)](CHANGELOG.md)
[![中文](https://img.shields.io/badge/中文-red)](CHANGELOG_CN.md)

xiaozhi-android-native的所有重要更改都将记录在此文件中。

**Language / 语言**: [English](CHANGELOG.md) | [中文](CHANGELOG_CN.md)

格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.0.0/)，
本项目遵循 [语义化版本](https://semver.org/lang/zh-CN/)。

## [未发布]

### 新增
- 使用SherpaOnnx的语音唤醒功能
- 本地关键词识别功能
- 16KB页面大小兼容性支持
- 全面的文档结构
- 开源项目设置

### 更改
- 优化音频处理管道
- 改进WebSocket连接稳定性
- 增强语音交互性能
- 更新构建配置以提高兼容性

### 修复
- 音频播放卡顿问题
- WebSocket连接可靠性
- UI更新中的线程安全
- 音频处理中的内存管理
- 重复消息显示问题

## [1.0.0] - 2025-01-12

### 新增
- xiaozhi-android-native的初始发布
- 双对话模式（Dify HTTP + 小智WebSocket）
- 实时语音通信
- 使用Room数据库的数据持久化
- 配置管理系统
- 使用Opus编解码器的音频处理
- 语音活动检测
- 对话历史管理
- 主题支持
- 音量控制集成

### 技术特性
- 使用Repository模式的MVVM架构
- Kotlin 100%代码库
- 使用ViewBinding的Android Views
- 用于本地存储的Room数据库
- OkHttp + Retrofit + WebSocket网络
- 用于音频处理的MediaRecorder + AudioTrack
- 用于Opus编解码器的Concentus库
- 用于语音识别的SherpaOnnx

### 文档
- 全面的项目文档
- 架构图和指南
- 开发和测试指南
- 错误修复文档
- 性能优化指南

---

## 版本历史

### v1.0.0 (初始发布)
- 完整的AI助手功能
- 语音交互功能
- 数据持久化和管理
- 开源项目结构

---

## 贡献

有关如何为此项目贡献的详细信息，请参阅 [CONTRIBUTING.md](CONTRIBUTING.md)。

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。
