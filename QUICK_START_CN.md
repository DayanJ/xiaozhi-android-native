# 快速开始指南

[![English](https://img.shields.io/badge/English-blue)](QUICK_START.md)
[![中文](https://img.shields.io/badge/中文-red)](QUICK_START_CN.md)

**Language / 语言**: [English](QUICK_START.md) | [中文](QUICK_START_CN.md)

## 🚀 5分钟快速上手

### 1. 环境准备
确保您的开发环境满足以下要求：
- **Android Studio**: 最新版本（推荐Arctic Fox或更新版本）
- **JDK**: 8或更高版本
- **Android SDK**: API 24+ (Android 7.0+)
- **设备**: 带麦克风和扬声器的Android设备

### 2. 获取代码
```bash
# 克隆仓库
git clone https://github.com/DayanJ/xiaozhi-android-native.git
cd xiaozhi-android-native

# 或者直接下载ZIP文件
```

### 3. 打开项目
1. 启动Android Studio
2. 选择 "Open an existing project"
3. 导航到 `xiaozhi-android-native` 目录
4. 点击 "OK"
5. 等待Gradle同步完成（首次可能需要几分钟）

### 4. 配置项目
#### 4.1 检查SDK设置
- 确保Android SDK已正确安装
- 检查 `local.properties` 文件中的SDK路径

#### 4.2 配置权限
应用需要以下权限（已在AndroidManifest.xml中声明）：
- `RECORD_AUDIO` - 语音录制
- `INTERNET` - 网络通信
- `MODIFY_AUDIO_SETTINGS` - 音频设置

### 5. 运行应用
#### 5.1 连接设备
- 通过USB连接Android设备
- 启用开发者选项和USB调试
- 或在Android Studio中启动模拟器

#### 5.2 构建和运行
1. 点击Android Studio工具栏中的 "Run" 按钮
2. 选择目标设备
3. 等待应用安装和启动

### 6. 首次使用
#### 6.1 配置Dify服务
1. 打开应用设置
2. 选择 "Dify配置"
3. 添加您的Dify API配置：
   - 配置名称：自定义名称
   - API地址：您的Dify服务地址
   - API密钥：您的Dify API密钥

#### 6.2 配置小智服务
1. 在设置中选择 "小智配置"
2. 添加小智服务配置：
   - 配置名称：自定义名称
   - WebSocket地址：小智服务地址
   - MAC地址：设备MAC地址
   - Token：认证令牌

#### 6.3 开始对话
1. 点击主界面的 "+" 按钮
2. 选择对话类型：
   - **Dify对话**：文本对话，支持图片上传
   - **小智对话**：语音对话，支持实时语音交互
3. 开始与AI助手对话

## 🎯 核心功能体验

### 语音对话
1. 选择 "小智对话"
2. 点击麦克风按钮开始录音
3. 说话后松开按钮发送
4. 等待AI回复并播放

### 语音唤醒
1. 在小智对话界面
2. 说出 "小安小安" 唤醒词
3. 系统自动开始录音
4. 实现免手动操作的语音交互

### 文本对话
1. 选择 "Dify对话"
2. 在输入框中输入文字
3. 点击发送按钮
4. 支持图片上传功能

## 🔧 常见问题

### Q: 应用无法启动？
A: 检查以下项目：
- Android SDK是否正确安装
- 设备Android版本是否≥7.0
- 网络连接是否正常

### Q: 语音功能不工作？
A: 确保：
- 已授予麦克风权限
- 设备有可用的麦克风
- 网络连接正常（小智服务）

### Q: 无法连接到服务器？
A: 检查：
- 网络连接状态
- 服务器地址配置是否正确
- 防火墙设置

### Q: 音频播放有问题？
A: 尝试：
- 调整设备音量
- 检查音频权限
- 重启应用

## 📚 进一步学习

### 项目架构
- 查看 [架构文档](docs/02-architecture/)
- 了解MVVM模式实现
- 学习Repository模式应用

### 开发指南
- 阅读 [开发文档](docs/03-development/)
- 了解音频处理实现
- 学习WebSocket通信

### 问题排查
- 查看 [问题修复文档](docs/04-bug-fixes/)
- 了解常见问题解决方案
- 学习性能优化技巧

## 🤝 获取帮助

- **GitHub Issues**: [提交问题](https://github.com/DayanJ/xiaozhi-android-native/issues)
- **邮箱支持**: jingdayanw@gmail.com
- **文档中心**: [docs/](docs/)
- **贡献指南**: [CONTRIBUTING.md](CONTRIBUTING.md)

## 🎉 下一步

现在您已经成功运行了xiaozhi-android-native！您可以：

1. **探索功能**: 尝试不同的对话模式和功能
2. **阅读代码**: 了解项目架构和实现细节
3. **参与贡献**: 查看贡献指南，参与项目开发
4. **分享反馈**: 通过Issues或邮件提供反馈

祝您使用愉快！🚀
