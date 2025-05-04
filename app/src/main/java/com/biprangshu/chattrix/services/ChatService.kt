package com.biprangshu.chattrix.services

import com.biprangshu.chattrix.data.MessageModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import javax.inject.Inject

class ChatService @Inject constructor(
    private val database: FirebaseDatabase,
    private val auth: FirebaseAuth
) {

    //chatid for users
    fun getChatId(userId1: String, userId2: String): String {
        return if (userId1 < userId2){
            "$userId1-$userId2"
        }else{
            "$userId2-$userId1"
        }
    }

    //send message
    fun sendMessage(message: String, recieverId: String, onComplete: (Boolean)->Unit){
        val currentUser = auth.currentUser?: return
        val chatId= getChatId(currentUser.uid, recieverId)
        val messagesref= database.getReference("chats/$chatId/messages")

        val messageId=messagesref.push().key ?: return
        val timeStamp= System.currentTimeMillis()

        val messageModel= MessageModel(
            messageId = messageId,
            senderId = currentUser.uid,
            recieverId = recieverId,
            message = message,
            timestamp = timeStamp,
        )

        messagesref.child(messageId).setValue(messageModel)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }

    }

    // Mark message as read
    fun markMessageAsRead(chatId: String, messageId: String) {
        database.getReference("chats/$chatId/messages/$messageId/isRead")
            .setValue(true)
    }
}