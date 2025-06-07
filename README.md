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

## 🐛 Known Issues

- [ ] Message delivery status indicators (planned)
- [ ] Image/media sharing (in development)
- [ ] Group chat functionality (future release)

*Built with ❤️ using Jetpack Compose*
