package com.biprangshu.chattrix.data

data class MessageModel(
    val messageId: String = "",
    val senderId: String = "",
    val recieverId: String = "",
    val message: String = "",
    val timestamp: Long = 0,
    val isRead: Boolean = false
)