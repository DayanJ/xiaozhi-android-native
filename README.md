# xiaozhi-android-native

> A native Android AI assistant application with voice interaction capabilities, supporting Dify conversations and Xiaozhi voice chat.

## 🚀 Features

- **Dual Conversation Modes**: Dify HTTP conversations + Xiaozhi WebSocket voice chat
- **Real-time Voice Communication**: Voice recording, playback, and real-time interaction
- **Data Persistence**: Local storage for conversation history and configuration
- **Configuration Management**: Flexible Dify and Xiaozhi configuration management
- **Voice Wake-up**: Local keyword spotting using SherpaOnnx
- **Theme Support**: Customizable UI themes

## 📱 Screenshots

*Screenshots will be added here*

## 🛠️ Tech Stack

- **Architecture**: MVVM + Repository Pattern
- **Language**: Kotlin 100%
- **UI Framework**: Android Views + ViewBinding
- **Database**: Room (SQLite)
- **Network**: OkHttp + Retrofit + WebSocket
- **Audio**: MediaRecorder + AudioTrack + Opus codec
- **Voice Recognition**: SherpaOnnx (local keyword spotting)

## 📋 Requirements

- Android Studio (latest version)
- JDK 8 or higher
- Android SDK API 24+
- Android device with microphone and speaker

## 🚀 Quick Start

### 1. Clone the repository
```bash
git clone https://github.com/DayanJ/xiaozhi-android-native.git
cd xiaozhi-android-native
```

### 2. Open in Android Studio
1. Open Android Studio
2. Select "Open an existing project"
3. Navigate to the cloned directory
4. Wait for Gradle sync to complete

### 3. Configure the project
1. Update server configurations in the app
2. Configure Dify API endpoints
3. Set up Xiaozhi WebSocket connections

### 4. Run the application
1. Connect an Android device or start an emulator
2. Click the Run button in Android Studio
3. Grant necessary permissions (microphone, storage)

## 📁 Project Structure

```
app/src/main/java/com/lhht/aiassistant/
├── MainActivity.kt                    # Main entry point
├── model/                            # Data models
├── database/                         # Database layer
├── repository/                       # Data repository
├── service/                          # Business services
├── viewmodel/                        # State management
├── ui/                               # User interface
└── utils/                            # Utility classes
```

## 🔧 Configuration

### Dify Configuration
- Set your Dify API endpoint
- Configure API keys and authentication
- Set conversation parameters

### Xiaozhi Configuration
- Configure WebSocket server URL
- Set device identification
- Configure audio parameters

## 🎯 Core Features

### Voice Interaction
- Real-time voice recording and playback
- Opus audio codec for efficient transmission
- Voice activity detection
- Audio preprocessing and optimization

### Conversation Management
- Persistent conversation history
- Multi-conversation support
- Message threading and organization
- Export and import capabilities

### Voice Wake-up
- Local keyword spotting using SherpaOnnx
- Customizable wake words
- Low-latency voice activation
- Privacy-focused local processing

## 📚 Documentation

Comprehensive documentation is available in the [docs/](docs/) directory:

- 🚀 **[Getting Started](docs/01-getting-started/)** - New user guide
- 🏗️ **[Architecture](docs/02-architecture/)** - Project architecture and design
- 💻 **[Development](docs/03-development/)** - Development guides
- 🐛 **[Bug Fixes](docs/04-bug-fixes/)** - Issue fixes and solutions
- ⚡ **[Optimization](docs/05-optimization/)** - Performance optimization
- 🧪 **[Testing](docs/06-testing/)** - Testing and debugging
- 📊 **[Reports](docs/07-reports/)** - Analysis reports

## 🔄 Development

### Building the project
```bash
./gradlew assembleDebug
```

### Running tests
```bash
./gradlew test
```

### Code style
The project follows Android Kotlin style guidelines. Use `ktlint` for code formatting.

## 🤝 Contributing

We welcome contributions! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Contribution Guidelines
- Follow the existing code style
- Add tests for new features
- Update documentation as needed
- Ensure all tests pass

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- [SherpaOnnx](https://github.com/k2-fsa/sherpa-onnx) for voice recognition capabilities
- [Concentus](https://github.com/lostromb/concentus) for Opus audio codec
- [OkHttp](https://square.github.io/okhttp/) for network communication
- [Room](https://developer.android.com/training/data-storage/room) for local database

## 📞 Support

If you encounter any issues or have questions:

- Create an [Issue](https://github.com/your-username/xiaozhi-android-native/issues)
- Email: jingdayanw@gmail.com
- Check the [documentation](docs/)
- Review existing [bug fixes](docs/04-bug-fixes/)

## 🔗 Related Projects

This project is part of the Xiaozhi AI assistant ecosystem. Related projects include:
- [xiaozhi-esp32](https://github.com/your-username/xiaozhi-esp32) - ESP32 hardware implementation
- [xiaozhi-esp32-server-java](https://github.com/your-username/xiaozhi-esp32-server-java) - Backend server
- [xiaozhi-android-client](https://github.com/your-username/xiaozhi-android-client) - Flutter version

---

**Note**: This is the native Android implementation of the Xiaozhi AI assistant, providing optimal performance and user experience for voice interaction functionality.
