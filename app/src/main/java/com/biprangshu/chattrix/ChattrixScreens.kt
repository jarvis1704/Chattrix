package com.biprangshu.chattrix

object ChattrixScreens {
    const val LOGIN_SCREEN="loginscreen"
    const val HOME_SCREEN="homescreen"
    const val PROFILE_SCREEN = "profilescreen"
    const val CHAT_SCREEN = "chatscreen/{userId}"

    // Helper function to create chat screen route with userId
    fun chatScreen(userId: String) = "chatscreen/$userId"
}
