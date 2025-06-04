package com.biprangshu.chattrix

object ChattrixScreens {
    const val LOGIN_SCREEN = "loginscreen"
    const val HOME_SCREEN = "homescreen"
    const val PROFILE_SCREEN = "profilescreen"
    const val CHAT_SCREEN = "chatscreen"
    const val NEW_CHAT_SCREEN= "newchatscreen"
    const val EDIT_PROFILE_SCREEN = "editprofilescreen"

    // Helper function to create chat screen route with userId and userName
    fun chatScreenRoute(userId: String, userName: String) = "$CHAT_SCREEN/$userId/$userName"
}
