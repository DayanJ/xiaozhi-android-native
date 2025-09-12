# xiaozhi-android-native 文档中心

## 📚 文档分类

本项目文档已按功能分类整理，便于快速查找和使用。

### 🚀 01-快速开始 (Getting Started)
新手入门和项目基础信息

- **[README.md](01-getting-started/README.md)** - 项目基本介绍和功能特性
- **[QUICK_START_GUIDE.md](01-getting-started/QUICK_START_GUIDE.md)** - 5分钟快速上手指南
- **[COMPILE_GUIDE.md](01-getting-started/COMPILE_GUIDE.md)** - 项目编译指南
- **[verify_build.md](01-getting-started/verify_build.md)** - 编译验证说明

### 🏗️ 02-架构设计 (Architecture)
项目架构和设计文档

- **[PROJECT_ANALYSIS_GUIDE.md](02-architecture/PROJECT_ANALYSIS_GUIDE.md)** - 详细的项目结构分析
- **[ARCHITECTURE_DIAGRAMS.md](02-architecture/ARCHITECTURE_DIAGRAMS.md)** - 架构图和流程图
- **[PROJECT_SUMMARY.md](02-architecture/PROJECT_SUMMARY.md)** - 项目总结和概览
- **[CONVERSATION_PERSISTENCE_IMPLEMENTATION.md](02-architecture/CONVERSATION_PERSISTENCE_IMPLEMENTATION.md)** - 对话持久化实现
- **[REAL_TIME_VOICE_STREAMING_IMPLEMENTATION.md](02-architecture/REAL_TIME_VOICE_STREAMING_IMPLEMENTATION.md)** - 实时语音流实现

### 💻 03-开发指南 (Development)
开发相关指南和最佳实践

- **[OPUS_INTEGRATION_GUIDE.md](03-development/OPUS_INTEGRATION_GUIDE.md)** - Opus音频编解码集成指南
- **[CONCENTUS_INTEGRATION_COMPLETE.md](03-development/CONCENTUS_INTEGRATION_COMPLETE.md)** - Concentus库集成完成报告
- **[CONFIG_MIGRATION_TO_ROOM_COMPLETE.md](03-development/CONFIG_MIGRATION_TO_ROOM_COMPLETE.md)** - 配置迁移到Room数据库
- **[CONVERSATION_HISTORY_USAGE_GUIDE.md](03-development/CONVERSATION_HISTORY_USAGE_GUIDE.md)** - 对话历史使用指南
- **[VOLUME_CONTROL_BEST_PRACTICES_IMPROVEMENT.md](03-development/VOLUME_CONTROL_BEST_PRACTICES_IMPROVEMENT.md)** - 音量控制最佳实践改进

### 🐛 04-问题修复 (Bug Fixes)
各种问题修复和解决方案

#### 音频相关问题
- **[AUDIO_ISSUES_FIX_SUMMARY.md](04-bug-fixes/AUDIO_ISSUES_FIX_SUMMARY.md)** - 音频问题修复总结
- **[AUDIO_PLAYBACK_FIX_REPORT.md](04-bug-fixes/AUDIO_PLAYBACK_FIX_REPORT.md)** - 音频播放修复报告
- **[AUDIO_STUTTERING_FIX_SUMMARY.md](04-bug-fixes/AUDIO_STUTTERING_FIX_SUMMARY.md)** - 音频卡顿修复总结
- **[AUDIO_TRACK_AND_DELETE_FIX_REPORT.md](04-bug-fixes/AUDIO_TRACK_AND_DELETE_FIX_REPORT.md)** - AudioTrack和删除功能修复
- **[AUDIOTRACK_INITIALIZATION_FIX_REPORT.md](04-bug-fixes/AUDIOTRACK_INITIALIZATION_FIX_REPORT.md)** - AudioTrack初始化修复

#### WebSocket相关问题
- **[WEBSOCKET_ISSUES_FIX_SUMMARY.md](04-bug-fixes/WEBSOCKET_ISSUES_FIX_SUMMARY.md)** - WebSocket问题修复总结
- **[WEBSOCKET_STABILITY_AND_STT_MESSAGE_FIX.md](04-bug-fixes/WEBSOCKET_STABILITY_AND_STT_MESSAGE_FIX.md)** - WebSocket稳定性和STT消息修复

#### 配置和连接问题
- **[CONNECTION_STATE_REFRESH_FIX.md](04-bug-fixes/CONNECTION_STATE_REFRESH_FIX.md)** - 连接状态刷新修复
- **[CONVERSATION_LIST_REFRESH_FIX.md](04-bug-fixes/CONVERSATION_LIST_REFRESH_FIX.md)** - 对话列表刷新修复
- **[FIX_CONFIG_OVERWRITE_ISSUE.md](04-bug-fixes/FIX_CONFIG_OVERWRITE_ISSUE.md)** - 配置覆盖问题修复

#### 消息和显示问题
- **[DUPLICATE_MESSAGE_FIX.md](04-bug-fixes/DUPLICATE_MESSAGE_FIX.md)** - 重复消息修复
- **[MULTI_SENTENCE_RESPONSE_FIX.md](04-bug-fixes/MULTI_SENTENCE_RESPONSE_FIX.md)** - 多句回复修复
- **[REAL_TIME_MESSAGE_DISPLAY_FIX.md](04-bug-fixes/REAL_TIME_MESSAGE_DISPLAY_FIX.md)** - 实时消息显示修复

#### 其他问题
- **[THREAD_SAFETY_FIX_REPORT.md](04-bug-fixes/THREAD_SAFETY_FIX_REPORT.md)** - 线程安全修复报告
- **[VOLUME_CONTROL_SYNC_FIX.md](04-bug-fixes/VOLUME_CONTROL_SYNC_FIX.md)** - 音量控制同步修复
- **[JOB_CANCELLATION_EXCEPTION_FIX.md](04-bug-fixes/JOB_CANCELLATION_EXCEPTION_FIX.md)** - 任务取消异常修复

### ⚡ 05-性能优化 (Optimization)
性能优化相关文档

- **[WEBSOCKET_AND_VOICE_OPTIMIZATION_REPORT.md](05-optimization/WEBSOCKET_AND_VOICE_OPTIMIZATION_REPORT.md)** - WebSocket和语音优化报告
- **[WEBSOCKET_OPTIMIZATION_SUMMARY.md](05-optimization/WEBSOCKET_OPTIMIZATION_SUMMARY.md)** - WebSocket优化总结

### 🧪 06-测试相关 (Testing)
测试和调试相关文档

- **[TEST_CONFIG_LIST_REFRESH.md](06-testing/TEST_CONFIG_LIST_REFRESH.md)** - 配置列表刷新测试
- **[TEST_MAC_ADDRESS_AND_WEBSOCKET.md](06-testing/TEST_MAC_ADDRESS_AND_WEBSOCKET.md)** - MAC地址和WebSocket测试
- **[TEST_XIAOZHI_CONFIG_ENHANCEMENT.md](06-testing/TEST_XIAOZHI_CONFIG_ENHANCEMENT.md)** - 小智配置增强测试

### 📊 07-报告分析 (Reports)
各种分析报告和总结

- **[960_BYTE_DATA_ANALYSIS.md](07-reports/960_BYTE_DATA_ANALYSIS.md)** - 960字节数据分析
- **[SETTINGS_FUNCTIONALITY_REPORT.md](07-reports/SETTINGS_FUNCTIONALITY_REPORT.md)** - 设置功能报告
- **[BUILD_SUCCESS.md](07-reports/BUILD_SUCCESS.md)** - 构建成功报告

## 🎯 快速导航

### 新手入门
1. 阅读 [README.md](01-getting-started/README.md) 了解项目
2. 按照 [QUICK_START_GUIDE.md](01-getting-started/QUICK_START_GUIDE.md) 快速上手
3. 参考 [COMPILE_GUIDE.md](01-getting-started/COMPILE_GUIDE.md) 编译项目

### 开发者指南
1. 学习 [PROJECT_ANALYSIS_GUIDE.md](02-architecture/PROJECT_ANALYSIS_GUIDE.md) 理解架构
2. 查看 [ARCHITECTURE_DIAGRAMS.md](02-architecture/ARCHITECTURE_DIAGRAMS.md) 了解设计
3. 参考 [03-development/](03-development/) 目录下的开发指南

### 问题排查
1. 查看 [04-bug-fixes/](04-bug-fixes/) 目录下的修复文档
2. 参考 [05-optimization/](05-optimization/) 目录下的优化方案
3. 使用 [06-testing/](06-testing/) 目录下的测试方法

### 项目分析
1. 阅读 [PROJECT_SUMMARY.md](02-architecture/PROJECT_SUMMARY.md) 了解项目概况
2. 查看 [07-reports/](07-reports/) 目录下的分析报告
3. 参考各种实现文档了解技术细节

## 📝 文档维护

- 所有文档使用Markdown格式
- 文档按功能分类存放
- 定期更新文档内容
- 保持文档与代码同步

## 🔍 搜索技巧

- 使用 `Ctrl+F` 在文档中搜索关键词
- 根据文件名快速定位相关文档
- 查看文档目录了解整体结构
- 使用Git历史查看文档变更

---

**提示**: 建议新手从 `01-getting-started` 目录开始，开发者重点关注 `02-architecture` 和 `03-development` 目录，遇到问题时查看 `04-bug-fixes` 目录。
