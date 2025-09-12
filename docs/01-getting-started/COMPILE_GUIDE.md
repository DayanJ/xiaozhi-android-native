# Android项目编译指南

## 编译方法

### 方法1: 使用Android Studio (推荐)

1. 打开Android Studio
2. 选择 "Open an existing Android Studio project"
3. 选择 `android-native-app` 文件夹
4. 等待Gradle同步完成
5. 连接Android设备或启动模拟器
6. 点击运行按钮

### 方法2: 使用命令行

如果系统gradle有初始化脚本冲突，可以尝试以下方法：

#### 临时禁用初始化脚本
```bash
# 重命名初始化脚本
mv "D:\tools\gradle-9.0.0-bin\gradle-9.0.0\init.d\init.gradle" "D:\tools\gradle-9.0.0-bin\gradle-9.0.0\init.d\init.gradle.bak"

# 然后运行编译
gradle build
```

#### 使用不同的gradle版本
```bash
# 下载gradle 8.4
# 设置GRADLE_HOME环境变量指向gradle 8.4
# 然后运行编译
gradle build
```

#### 使用Android Studio的gradle wrapper
```bash
# 在Android Studio中打开项目后，使用其内置的gradle wrapper
./gradlew build
```

## 项目结构

```
android-native-app/
├── app/
│   ├── src/main/
│   │   ├── java/com/lhht/aiassistant/
│   │   │   ├── MainActivity.kt
│   │   │   ├── model/          # 数据模型
│   │   │   ├── service/        # 服务层
│   │   │   ├── viewmodel/      # 状态管理
│   │   │   ├── ui/            # UI界面
│   │   │   └── utils/         # 工具类
│   │   ├── res/               # 资源文件
│   │   └── AndroidManifest.xml
│   └── build.gradle
├── build.gradle
├── settings.gradle
├── gradle.properties
└── README.md
```

## 依赖库

项目使用以下主要依赖库：

- **AndroidX**: 核心Android库
- **Material Design**: UI组件
- **OkHttp + Retrofit**: 网络请求
- **Gson**: JSON解析
- **Glide**: 图片加载
- **Dexter**: 权限管理

## 编译要求

- Android Studio Arctic Fox 或更高版本
- JDK 8 或更高版本
- Android SDK API 24 或更高版本
- Gradle 8.4

## 常见问题

### 1. Gradle初始化脚本冲突
**问题**: Build was configured to prefer settings repositories over project repositories but repository 'maven' was added by initialization script

**解决方案**: 
- 使用Android Studio打开项目
- 或者临时重命名系统的gradle初始化脚本

### 2. 找不到gradle-wrapper.jar
**问题**: 找不到或无法加载主类 org.gradle.wrapper.GradleWrapperMain

**解决方案**:
- 使用Android Studio打开项目，它会自动下载gradle wrapper
- 或者手动下载gradle-wrapper.jar文件

### 3. 资源文件找不到
**问题**: 编译时找不到R文件

**解决方案**:
- 确保所有资源文件都在正确的目录中
- 检查资源文件是否有语法错误

## 功能验证

编译成功后，应用应该包含以下功能：

1. **主界面**: 显示对话列表
2. **对话类型选择**: 选择Dify或小智对话
3. **聊天界面**: 发送和接收消息
4. **语音通话**: 小智对话的语音功能
5. **设置界面**: 配置管理
6. **配置编辑**: 添加和编辑配置

## 注意事项

1. 首次运行需要配置Dify或小智服务
2. 语音功能需要麦克风权限
3. 图片上传需要存储权限
4. 网络连接是必需的

如果遇到编译问题，建议使用Android Studio打开项目，它会自动处理大部分配置问题。
