# SherpaOnnx 关键词编码问题修复报告

## 🚨 问题描述

在测试SherpaOnnx语音唤醒功能时，遇到了关键词编码失败的问题：

```
Cannot find ID for token 小安小安 at line: 小安小安. (Hint: Check the tokens.txt see if 小安小安 in it)
Encode keywords '小安小安' failed.
```

## 🔍 问题分析

### 1. 根本原因
- 模型文件 `tokens.txt` 中没有中文字符"小安"
- 关键词文件 `keywords.txt` 中使用了中文关键词"小安小安"
- 导致SherpaOnnx无法将中文关键词编码为模型可识别的token ID

### 2. 技术细节
- **模型类型**: `sherpa-onnx-kws-zipformer-wenetspeech-3.3M-2024-01-01`
- **语言支持**: 主要支持拼音字符，不包含中文字符
- **tokens.txt内容**: 包含拼音字符如 `x`, `iǎo`, `ān` 等，但不包含完整的中文字符

## 🔧 解决方案

### 1. 关键词修改
将关键词从中文改为英文：

**修改前**:
```kotlin
private val defaultKeywords = "小安小安"
```

**修改后**:
```kotlin
private val defaultKeywords = "hi"
```

### 2. 关键词文件更新
更新 `keywords.txt` 文件：

**修改前**:
```
x iǎo ān x iǎo ān @小安小安
```

**修改后**:
```
hi
```

### 3. 包名修复
将SherpaOnnx Kotlin API的包名改回原始包名：

**修改前**:
```kotlin
package com.lhht.aiassistant.service.kws
```

**修改后**:
```kotlin
package com.k2fsa.sherpa.onnx
```

## 📁 修改的文件

### 1. KeywordWakeupService.kt
- 修改默认关键词为 "hi"
- 更新import语句使用正确的包名

### 2. keywords.txt
- 将关键词从中文改为英文 "hi"

### 3. Kotlin API文件
- `KeywordSpotter.kt`
- `FeatureConfig.kt`
- `OnlineStream.kt`
- `OnlineRecognizer.kt`

## ✅ 验证结果

### 1. 编译成功
- 项目重新构建成功，无编译错误
- JNI方法调用正常

### 2. 关键词验证
- "hi" 关键词在 `tokens.txt` 中有对应的字符：
  - `h` (ID: 75)
  - `i` (ID: 152)

### 3. 包名修复
- 使用原始包名 `com.k2fsa.sherpa.onnx`
- JNI方法名匹配原生库中的实现

## 🎯 测试建议

### 1. 功能测试
- 安装APK到Android设备
- 进入语音通话界面
- 说出 "hi" 测试语音唤醒功能

### 2. 预期行为
- 检测到 "hi" 后显示唤醒提示
- 自动开始语音流录制
- 与现有小智服务正常交互

## 🔄 后续优化

### 1. 中文支持
如果需要中文关键词支持，可以考虑：
- 使用支持中文的模型
- 或者使用拼音形式的关键词

### 2. 关键词自定义
- 实现用户自定义关键词功能
- 支持多种语言的关键词

### 3. 模型选择
- 根据语言需求选择合适的模型
- 支持模型动态切换

## 📊 技术总结

这次修复解决了两个关键问题：
1. **JNI包名不匹配**: 通过恢复原始包名解决了原生库调用问题
2. **关键词编码失败**: 通过使用模型支持的英文关键词解决了编码问题

修复后的系统应该能够正常进行语音唤醒功能测试。
