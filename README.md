# Chattrix 💬

A modern, real-time chat application built with Jetpack Compose and Firebase for Android.

## ✨ Features

- **Real-time Messaging**: Instant message delivery and synchronization
- **Modern UI**: Beautiful Material Design 3 interface with smooth animations
- **User Authentication**: Secure Firebase Authentication
- **Contact Management**: Browse and chat with available contacts
- **Message History**: Persistent chat history with timestamps
- **Responsive Design**: Adaptive layouts for different screen sizes
- **Haptic Feedback**: Enhanced user experience with tactile feedback
- **Status Indicators**: Read/unread message status

## 🚀 Screenshots

*Add your app screenshots here*

## 🛠️ Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM with Hilt Dependency Injection
- **Backend**: Firebase
  - Authentication
  - Firestore Database
  - Real-time Database
- **Navigation**: Navigation Compose
- **State Management**: StateFlow & Compose State
- **Design**: Material Design 3

## 📋 Prerequisites

- Android Studio Arctic Fox or later
- Minimum SDK: API 21 (Android 5.0)
- Target SDK: API 34
- Kotlin 1.8+
- Firebase Project Setup

## 🔧 Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/chattrix.git
   cd chattrix
   ```

2. **Firebase Setup**
   - Create a new project in [Firebase Console](https://console.firebase.google.com/)
   - Add an Android app to your Firebase project
   - Download `google-services.json` and place it in the `app/` directory
   - Enable Authentication and Firestore Database in Firebase Console

3. **Configure Firebase Authentication**
   - Go to Authentication > Sign-in method
   - Enable Email/Password authentication
   - Configure other sign-in methods as needed

4. **Configure Firestore Database**
   - Create a Firestore database
   - Set up security rules (see [Security Rules](#security-rules) section)

5. **Build and Run**
   - Open the project in Android Studio
   - Sync the project with Gradle files
   - Run the app on an emulator or physical device

## 🏗️ Project Structure

```
app/
├── src/main/java/com/biprangshu/chattrix/
│   ├── authentication/          # Authentication related classes
│   ├── data/                   # Data models and repositories
│   ├── home/                   # Main chat screens
│   │   ├── HomeScreen.kt       # Main chat list screen
│   │   ├── ChatScreen.kt       # Individual chat screen
│   │   └── NewChatScreen.kt    # Contact selection screen
│   ├── ui/theme/               # UI theme and styling
│   ├── uiutils/                # Reusable UI components
│   └── viewmodel/              # ViewModels for state management
└── res/                        # Resources (layouts, strings, etc.)
```

## 🔐 Security Rules

Add these Firestore security rules to your Firebase project:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users can read/write their own user document
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Chat messages - users can only access chats they're part of
    match /chats/{chatId} {
      allow read, write: if request.auth != null && 
        (request.auth.uid in resource.data.participants);
    }
    
    // Messages within chats
    match /chats/{chatId}/messages/{messageId} {
      allow read, write: if request.auth != null;
    }
  }
}
```

## 🎨 Key Components

### HomeScreen
- Displays list of recent chats
- Shows unread message indicators
- Floating Action Button for new chats
- User profile access

### ChatScreen
- Real-time message display
- Message input with send button
- Animated send button interactions
- Message timestamps and status

### NewChatScreen
- Contact list display
- Search and filter functionality
- Easy contact selection

## 🔄 State Management

The app uses MVVM architecture with:
- **ViewModels** for business logic and state management
- **StateFlow** for reactive data streams
- **Compose State** for UI state management
- **Hilt** for dependency injection

## 🎯 Usage

1. **Sign Up/Login**: Create an account or login with existing credentials
2. **View Chats**: Browse your recent conversations on the home screen
3. **Start New Chat**: Tap the '+' button to select a contact and start chatting
4. **Send Messages**: Type and send messages in real-time
5. **Profile**: Access your profile from the home screen

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Code Style
- Follow Kotlin coding conventions
- Use meaningful variable and function names
- Add comments for complex logic
- Ensure proper error handling

## 🐛 Known Issues

- [ ] Message delivery status indicators (planned)
- [ ] Image/media sharing (in development)
- [ ] Group chat functionality (future release)

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Author

**Biprangshu**
- GitHub: [@yourusername](https://github.com/yourusername)
- Email: your.email@example.com

## 🙏 Acknowledgments

- Firebase team for excellent backend services
- Jetpack Compose team for modern UI toolkit
- Material Design team for design guidelines
- Android development community for inspiration

## 📱 Download

*Add Google Play Store badge and link when published*

## 🔄 Version History

- **v1.0.0** - Initial release with core chat functionality
- **v1.1.0** - Enhanced UI and performance improvements (planned)
- **v2.0.0** - Group chat and media sharing (planned)

---

*Built with ❤️ using Jetpack Compose*
