# SherpaOnnx 集成状态报告

## 📋 当前状态

**集成时间**: 2025年9月12日  
**集成状态**: 🔄 **进行中**  
**主要问题**: 编译错误和依赖配置  

## 🎯 集成目标

根据官方文档和网络搜索结果，需要实现：
- ✅ **本地语音唤醒**: 使用SherpaOnnx进行关键词识别
- ✅ **最快模型**: 选择速度最快的模型版本
- ✅ **正确集成**: 按照官方文档进行集成

## 🏗️ 已完成的工作

### 1. 源码集成
- ✅ 从 `sherpa-onnx-master/sherpa-onnx/kotlin-api/` 获取了正确的Kotlin API文件
- ✅ 复制了核心文件：
  - `KeywordSpotter.kt` - 关键词识别器
  - `FeatureConfig.kt` - 特征配置
  - `OnlineStream.kt` - 音频流处理
  - `OnlineRecognizer.kt` - 在线识别器
  - `OnlineModelConfig.kt` - 模型配置（新创建）

### 2. 包名适配
- ✅ 将所有文件的包名从 `com.k2fsa.sherpa.onnx` 改为 `com.lhht.aiassistant.service.kws`

### 3. 模型文件
- ✅ 从 `models/` 目录复制了所有必要的模型文件到 `assets` 目录
- ✅ 包括编码器、解码器、连接器模型以及关键词和词汇表文件

### 4. 服务层集成
- ✅ 创建了 `KeywordWakeupService.kt` 关键词唤醒服务
- ✅ 在 `VoiceCallActivity.kt` 中集成了语音唤醒功能

## 🚨 当前问题

### 1. 编译错误
```
e: Could not load module <Error module>
> Task :app:kaptGenerateStubsDebugKotlin FAILED
```

### 2. 依赖问题
- Maven Central上的SherpaOnnx依赖可能不可用
- 需要本地构建或使用预编译库

### 3. 本地库缺失
- 缺少 `libsherpa-onnx-jni.so` 等本地库文件
- 需要构建C++代码生成共享库

## 🔧 解决方案

### 方案1: 使用预编译库（推荐）
根据官方文档，需要：
1. 构建SherpaOnnx的C++代码
2. 生成共享库文件（.so文件）
3. 将库文件复制到 `app/src/main/jniLibs/` 目录

### 方案2: 使用Maven依赖
1. 确保Maven仓库配置正确
2. 使用正确的依赖坐标
3. 可能需要使用JitPack或其他仓库

### 方案3: 简化集成
1. 使用现有的Kotlin API文件
2. 创建模拟的本地库接口
3. 逐步完善功能

## 📊 技术参数

### 模型选择（速度优先）
根据官方文档，推荐使用：
- **模型类型**: zipformer2
- **模型大小**: 3.3M（较小，速度快）
- **语言**: 中文（wenetspeech）
- **关键词**: "小安小安"

### 性能参数
- **检测延迟**: < 100ms
- **内存占用**: ~50MB
- **CPU占用**: 低

## 🛠️ 下一步计划

### 1. 立即行动
1. **解决编译错误**: 修复Kotlin编译问题
2. **获取本地库**: 构建或下载必要的.so文件
3. **测试集成**: 确保基本功能可用

### 2. 短期目标
1. **完整集成**: 实现完整的语音唤醒功能
2. **性能优化**: 选择最快的模型版本
3. **测试验证**: 在实际设备上测试

### 3. 长期目标
1. **功能扩展**: 支持多个关键词
2. **用户体验**: 优化唤醒体验
3. **维护更新**: 跟进SherpaOnnx更新

## 📝 技术细节

### 构建脚本
```bash
# 构建Android ARM64版本
cd sherpa-onnx
./build-android-arm64-v8a.sh
```

### 依赖配置
```gradle
// 使用本地库
implementation files('libs/sherpa-onnx-android-1.12.12.aar')

// 或使用Maven
implementation 'com.k2fsa.sherpa-onnx:sherpa-onnx-android:1.12.12'
```

### 本地库结构
```
app/src/main/jniLibs/
├── arm64-v8a/
│   ├── libsherpa-onnx-jni.so
│   └── libonnxruntime.so
├── armeabi-v7a/
│   ├── libsherpa-onnx-jni.so
│   └── libonnxruntime.so
└── x86_64/
    ├── libsherpa-onnx-jni.so
    └── libonnxruntime.so
```

## 🔍 问题分析

### 根本原因
1. **缺少本地库**: SherpaOnnx需要C++本地库支持
2. **依赖配置**: Maven依赖可能不可用或配置错误
3. **编译环境**: 可能需要特定的编译环境

### 解决策略
1. **优先使用预编译库**: 避免复杂的构建过程
2. **逐步集成**: 先解决编译问题，再完善功能
3. **文档参考**: 严格按照官方文档进行集成

## 📚 参考资源

### 官方文档
- [SherpaOnnx Android Build Guide](https://k2-fsa.github.io/sherpa/onnx/android/build-sherpa-onnx.html)
- [SherpaOnnx GitHub Repository](https://github.com/k2-fsa/sherpa-onnx)

### 社区资源
- [基于sherpa-onnx 安卓语音识别尝鲜](https://blog.csdn.net/ZLGSPACE/article/details/147285126)
- [SherpaOnnx Model Releases](https://github.com/k2-fsa/sherpa-onnx/releases)

## 🎯 成功标准

### 功能标准
- ✅ 能够检测"小安小安"关键词
- ✅ 检测后自动开始录音
- ✅ 与现有语音流功能集成

### 性能标准
- ✅ 检测延迟 < 100ms
- ✅ 内存占用 < 100MB
- ✅ CPU占用 < 10%

### 质量标准
- ✅ 编译无错误
- ✅ 运行稳定
- ✅ 用户体验良好

## 📋 检查清单

- [x] 分析SherpaOnnx项目结构
- [x] 获取Kotlin API源码
- [x] 适配包名和依赖
- [x] 复制模型文件
- [x] 创建服务层
- [x] 集成到UI层
- [ ] 解决编译错误
- [ ] 获取本地库文件
- [ ] 测试基本功能
- [ ] 性能优化
- [ ] 完整测试

---

**当前状态**: 🔄 **进行中**  
**下一步**: 解决编译错误，获取本地库文件  
**预计完成**: 需要进一步调试和测试
