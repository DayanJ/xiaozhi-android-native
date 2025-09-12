# Contributing to xiaozhi-android-native

Thank you for your interest in contributing to xiaozhi-android-native! This document provides guidelines and information for contributors.

## 🤝 How to Contribute

### Reporting Issues
- Use the GitHub issue tracker to report bugs
- Provide detailed information about the issue
- Include steps to reproduce the problem
- Specify your device and Android version

### Suggesting Features
- Open an issue with the "enhancement" label
- Describe the feature and its benefits
- Consider the impact on existing functionality

### Code Contributions
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## 📋 Development Setup

### Prerequisites
- Android Studio (latest version)
- JDK 8 or higher
- Android SDK API 24+
- Git

### Setup Steps
1. Fork and clone the repository
2. Open the project in Android Studio
3. Wait for Gradle sync to complete
4. Run the project to ensure it builds successfully

## 🎯 Code Style Guidelines

### Kotlin Style
- Follow [Android Kotlin style guide](https://developer.android.com/kotlin/style-guide)
- Use `ktlint` for code formatting
- Prefer immutable data structures
- Use meaningful variable and function names

### Architecture
- Follow MVVM pattern
- Use Repository pattern for data access
- Implement proper separation of concerns
- Use dependency injection where appropriate

### Documentation
- Add KDoc comments for public APIs
- Update README.md for significant changes
- Document complex algorithms and business logic

## 🧪 Testing

### Unit Tests
- Write unit tests for business logic
- Test edge cases and error conditions
- Maintain good test coverage

### Integration Tests
- Test database operations
- Test network interactions
- Test audio processing functionality

### Manual Testing
- Test on different Android versions
- Test on different device sizes
- Test voice interaction features

## 📝 Commit Guidelines

### Commit Message Format
```
type(scope): description

[optional body]

[optional footer]
```

### Types
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes
- `refactor`: Code refactoring
- `test`: Test additions or changes
- `chore`: Build or tool changes

### Examples
```
feat(voice): add voice wake-up functionality
fix(audio): resolve audio playback stuttering
docs(readme): update installation instructions
```

## 🔍 Pull Request Process

### Before Submitting
1. Ensure all tests pass
2. Update documentation if needed
3. Rebase your branch on the latest main
4. Squash commits if necessary

### PR Description
- Describe what changes were made
- Explain why the changes were necessary
- Reference any related issues
- Include screenshots for UI changes

### Review Process
- All PRs require review
- Address review comments promptly
- Make requested changes in new commits
- Keep the PR focused and atomic

## 🐛 Bug Reports

### Required Information
- Android version
- Device model
- App version
- Steps to reproduce
- Expected vs actual behavior
- Logs (if applicable)

### Bug Report Template
```markdown
**Bug Description**
A clear description of the bug.

**Steps to Reproduce**
1. Go to '...'
2. Click on '....'
3. See error

**Expected Behavior**
What you expected to happen.

**Actual Behavior**
What actually happened.

**Environment**
- Android Version: [e.g., 12]
- Device: [e.g., Pixel 6]
- App Version: [e.g., 1.0.0]

**Additional Context**
Any other context about the problem.
```

## ✨ Feature Requests

### Feature Request Template
```markdown
**Feature Description**
A clear description of the feature.

**Use Case**
Why is this feature needed?

**Proposed Solution**
How should this feature work?

**Alternatives Considered**
Other solutions you've considered.

**Additional Context**
Any other context about the feature request.
```

## 📚 Documentation

### Code Documentation
- Use KDoc for public APIs
- Include parameter descriptions
- Document return values
- Add usage examples

### User Documentation
- Update README.md for new features
- Add screenshots for UI changes
- Document configuration options
- Provide troubleshooting guides

## 🔒 Security

### Security Issues
- Report security vulnerabilities privately
- Use GitHub's security advisory feature
- Do not disclose vulnerabilities publicly until fixed

### Code Security
- Validate all user inputs
- Use secure network communication
- Follow Android security best practices
- Regular dependency updates

## 🏷️ Labels

We use the following labels for issues and PRs:

- `bug`: Something isn't working
- `enhancement`: New feature or request
- `documentation`: Improvements to documentation
- `good first issue`: Good for newcomers
- `help wanted`: Extra attention is needed
- `question`: Further information is requested
- `wontfix`: This will not be worked on

## 📞 Getting Help

### Community
- GitHub Discussions for questions
- GitHub Issues for bug reports
- Pull Requests for code contributions
- Email: jingdayanw@gmail.com

### Resources
- [Android Developer Documentation](https://developer.android.com/)
- [Kotlin Documentation](https://kotlinlang.org/docs/)
- [Project Documentation](docs/)

## 🎉 Recognition

Contributors will be recognized in:
- README.md contributors section
- Release notes
- GitHub contributors page

Thank you for contributing to xiaozhi-android-native! 🚀
