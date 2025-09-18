# Quick Start Guide

[![English](https://img.shields.io/badge/English-blue)](QUICK_START.md)
[![中文](https://img.shields.io/badge/中文-red)](QUICK_START_CN.md)

**Language / 语言**: [English](QUICK_START.md) | [中文](QUICK_START_CN.md)

## 🚀 5-Minute Quick Start

### 1. Environment Setup
Ensure your development environment meets the following requirements:
- **Android Studio**: Latest version (Arctic Fox or newer recommended)
- **JDK**: 8 or higher
- **Android SDK**: API 24+ (Android 7.0+), compileSdk 35, targetSdk 35
- **Gradle**: 8.5+
- **Device**: Android device with microphone and speaker

### 2. Get the Code
```bash
# Clone the repository
git clone https://github.com/DayanJ/xiaozhi-android-native.git
cd xiaozhi-android-native

# Or download ZIP file directly
```

### 3. Open Project
1. Launch Android Studio
2. Select "Open an existing project"
3. Navigate to `xiaozhi-android-native` directory
4. Click "OK"
5. Wait for Gradle sync to complete (may take a few minutes on first run)

### 4. Configure Project
#### 4.1 Check SDK Settings
- Ensure Android SDK is properly installed
- Check SDK path in `local.properties` file

#### 4.2 Configure Permissions
The app requires the following permissions (already declared in AndroidManifest.xml):
- `RECORD_AUDIO` - Voice recording
- `INTERNET` - Network communication
- `MODIFY_AUDIO_SETTINGS` - Audio settings

### 5. Run the App
#### 5.1 Connect Device
- Connect Android device via USB
- Enable Developer Options and USB Debugging
- Or launch emulator in Android Studio

#### 5.2 Build and Run
1. Click "Run" button in Android Studio toolbar
2. Select target device
3. Wait for app installation and launch

**Note**: The project has been optimized and should build successfully without any Lint errors. If you encounter build issues, try running `./gradlew clean assembleDebug` first.

### 6. First Use
#### 6.1 Configure Dify Service
1. Open app settings
2. Select "Dify Config"
3. Add your Dify API configuration:
   - Config Name: Custom name
   - API URL: Your Dify service address
   - API Key: Your Dify API key

#### 6.2 Configure Xiaozhi Service
1. Select "Xiaozhi Config" in settings
2. Add Xiaozhi service configuration:
   - Config Name: Custom name
   - WebSocket URL: Xiaozhi service address
   - MAC Address: Device MAC address
   - Token: Authentication token

#### 6.3 Start Conversation
1. Click "+" button on main interface
2. Select conversation type:
   - **Dify Chat**: Text conversation with image upload support
   - **Xiaozhi Chat**: Voice conversation with real-time voice interaction
3. Start chatting with AI assistant

## 🎯 Core Features Experience

### Voice Chat
1. Select "Xiaozhi Chat"
2. Click microphone button to start recording
3. Speak and release button to send
4. Wait for AI response and playback

### Voice Wake-up
1. In Xiaozhi chat interface
2. Say "x iǎo ān x iǎo ān" wake word
3. System automatically starts recording
4. Achieve hands-free voice interaction

### Text Chat
1. Select "Dify Chat"
2. Type text in input field
3. Click send button
4. Support image upload functionality

## 🔧 Common Issues

### Q: App won't start?
A: Check the following:
- Is Android SDK properly installed?
- Is device Android version ≥ 7.0?
- Is network connection normal?

### Q: Voice features not working?
A: Ensure:
- Microphone permission is granted
- Device has available microphone
- Network connection is normal (Xiaozhi service)

### Q: Cannot connect to server?
A: Check:
- Network connection status
- Server address configuration is correct
- Firewall settings

### Q: Audio playback issues?
A: Try:
- Adjust device volume
- Check audio permissions
- Restart the app

## 📚 Further Learning

### Project Architecture
- View [Architecture Documentation](docs/02-architecture/)
- Learn MVVM pattern implementation
- Study Repository pattern application

### Development Guide
- Read [Development Documentation](docs/03-development/)
- Learn audio processing implementation
- Study WebSocket communication

### Troubleshooting
- Check [Bug Fix Documentation](docs/04-bug-fixes/)
- Learn common issue solutions
- Study performance optimization techniques

## 🤝 Get Help

- **GitHub Issues**: [Submit Issues](https://github.com/DayanJ/xiaozhi-android-native/issues)
- **Email Support**: jingdayanw@gmail.com
- **Documentation Center**: [docs/](docs/)
- **Contributing Guide**: [CONTRIBUTING.md](CONTRIBUTING.md)

## 🎉 Next Steps

Now you have successfully run xiaozhi-android-native! You can:

1. **Explore Features**: Try different conversation modes and features
2. **Read Code**: Understand project architecture and implementation details
3. **Contribute**: Check contributing guide and participate in project development
4. **Share Feedback**: Provide feedback through Issues or email

Enjoy using it! 🚀
