# 为 xiaozhi-android-native 贡献

[![English](https://img.shields.io/badge/English-blue)](CONTRIBUTING.md)
[![中文](https://img.shields.io/badge/中文-red)](CONTRIBUTING_CN.md)

感谢您对 xiaozhi-android-native 项目的贡献兴趣！本文档为贡献者提供指南和信息。

**Language / 语言**: [English](CONTRIBUTING.md) | [中文](CONTRIBUTING_CN.md)

## 🤝 如何贡献

### 报告问题
- 使用GitHub问题跟踪器报告错误
- 提供问题的详细信息
- 包含重现问题的步骤
- 指定您的设备和Android版本

### 建议功能
- 使用"enhancement"标签打开问题
- 描述功能及其好处
- 考虑对现有功能的影响

### 代码贡献
1. Fork 仓库
2. 创建功能分支
3. 进行更改
4. 如适用，添加测试
5. 提交Pull Request

## 📋 开发环境设置

### 先决条件
- Android Studio (最新版本)
- JDK 8 或更高版本
- Android SDK API 24+
- Git

### 设置步骤
1. Fork并克隆仓库
2. 在Android Studio中打开项目
3. 等待Gradle同步完成
4. 运行项目以确保构建成功

## 🎯 代码风格指南

### Kotlin风格
- 遵循 [Android Kotlin风格指南](https://developer.android.com/kotlin/style-guide)
- 使用`ktlint`进行代码格式化
- 优先使用不可变数据结构
- 使用有意义的变量和函数名

### 架构
- 遵循MVVM模式
- 使用Repository模式进行数据访问
- 实现适当的关注点分离
- 在适当的地方使用依赖注入

### 文档
- 为公共API添加KDoc注释
- 为重大更改更新README.md
- 记录复杂的算法和业务逻辑

## 🧪 测试

### 单元测试
- 为业务逻辑编写单元测试
- 测试边缘情况和错误条件
- 保持良好的测试覆盖率

### 集成测试
- 测试数据库操作
- 测试网络交互
- 测试音频处理功能

### 手动测试
- 在不同Android版本上测试
- 在不同设备尺寸上测试
- 测试语音交互功能

## 📝 提交指南

### 提交消息格式
```
type(scope): description

[optional body]

[optional footer]
```

### 类型
- `feat`: 新功能
- `fix`: 错误修复
- `docs`: 文档更改
- `style`: 代码风格更改
- `refactor`: 代码重构
- `test`: 测试添加或更改
- `chore`: 构建或工具更改

### 示例
```
feat(voice): 添加语音唤醒功能
fix(audio): 解决音频播放卡顿问题
docs(readme): 更新安装说明
```

## 🔍 Pull Request流程

### 提交前
1. 确保所有测试通过
2. 根据需要更新文档
3. 将您的分支基于最新的main分支
4. 如有必要，压缩提交

### PR描述
- 描述进行了哪些更改
- 解释为什么需要这些更改
- 引用任何相关问题
- 为UI更改包含截图

### 审查流程
- 所有PR都需要审查
- 及时处理审查意见
- 在新提交中进行请求的更改
- 保持PR专注和原子性

## 🐛 错误报告

### 必需信息
- Android版本
- 设备型号
- 应用版本
- 重现步骤
- 预期与实际行为
- 日志（如适用）

### 错误报告模板
```markdown
**错误描述**
错误的清晰描述。

**重现步骤**
1. 转到'...'
2. 点击'....'
3. 看到错误

**预期行为**
您期望发生的事情。

**实际行为**
实际发生的事情。

**环境**
- Android版本: [例如，12]
- 设备: [例如，Pixel 6]
- 应用版本: [例如，1.0.0]

**其他上下文**
关于问题的任何其他上下文。
```

## ✨ 功能请求

### 功能请求模板
```markdown
**功能描述**
功能的清晰描述。

**用例**
为什么需要这个功能？

**建议解决方案**
这个功能应该如何工作？

**考虑的替代方案**
您考虑过的其他解决方案。

**其他上下文**
关于功能请求的任何其他上下文。
```

## 📚 文档

### 代码文档
- 为公共API使用KDoc
- 包含参数描述
- 记录返回值
- 添加使用示例

### 用户文档
- 为新功能更新README.md
- 为UI更改添加截图
- 记录配置选项
- 提供故障排除指南

## 🔒 安全

### 安全问题
- 私下报告安全漏洞
- 使用GitHub的安全咨询功能
- 在修复之前不要公开披露漏洞

### 代码安全
- 验证所有用户输入
- 使用安全的网络通信
- 遵循Android安全最佳实践
- 定期更新依赖项

## 🏷️ 标签

我们为问题和PR使用以下标签：

- `bug`: 某些东西不工作
- `enhancement`: 新功能或请求
- `documentation`: 文档改进
- `good first issue`: 适合新手的
- `help wanted`: 需要额外关注
- `question`: 需要进一步信息
- `wontfix`: 不会处理这个

## 📞 获取帮助

### 社区
- GitHub Discussions用于问题
- GitHub Issues用于错误报告
- Pull Requests用于代码贡献

### 资源
- [Android开发者文档](https://developer.android.com/)
- [Kotlin文档](https://kotlinlang.org/docs/)
- [项目文档](docs/)

## 🎉 认可

贡献者将在以下地方得到认可：
- README.md贡献者部分
- 发布说明
- GitHub贡献者页面

感谢您为 xiaozhi-android-native 贡献！🚀
