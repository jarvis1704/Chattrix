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

    private var _messageId: String = ""
    private var _chatId: String = ""

    //send message
    fun sendMessage(message: String, recieverId: String, onComplete: (Boolean)->Unit){
        val currentUser = auth.currentUser?: return
        val chatId= getChatId(currentUser.uid, recieverId)
        val messagesref= database.getReference("chats/$chatId/messages")

        val messageId=messagesref.push().key ?: return
        val timeStamp= System.currentTimeMillis()

        _messageId=messageId
        _chatId=chatId

        val messageModel= MessageModel(
            messageId = messageId,
            senderId = currentUser.uid,
            recieverId = recieverId,
            message = message,
            timestamp = timeStamp,
            isRead = false
        )

        messagesref.child(messageId).setValue(messageModel)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }

    }

    fun getMessageId(): String {
        return _messageId
    }

    fun getChatId(): String{
        return _chatId
    }


    fun markMessageAsRead(chatId: String, messageId: String) {
        database.getReference("chats/$chatId/messages/$messageId/isRead")
            .setValue(true)
    }


    fun markAllMessagesAsRead(chatId: String, currentUserId: String) {
        database.getReference("chats/$chatId/messages")
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val updates = mutableMapOf<String, Any>()
                    for (messageSnapshot in snapshot.children) {
                        val message = messageSnapshot.getValue(MessageModel::class.java)

                        if (message != null && message.senderId != currentUserId && message.isRead == false) {
                            updates["${messageSnapshot.key}/isRead"] = true
                        }
                    }
                    if (updates.isNotEmpty()) {
                        database.getReference("chats/$chatId/messages").updateChildren(updates)
                    }
                }
            }
    }
}