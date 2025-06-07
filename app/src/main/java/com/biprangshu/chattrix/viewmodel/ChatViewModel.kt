package com.biprangshu.chattrix.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.biprangshu.chattrix.data.MessageModel
import com.biprangshu.chattrix.services.ChatService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatService: ChatService,
    application: Application
): AndroidViewModel(application) {

    private val _messages= MutableStateFlow<List<MessageModel>>(emptyList())
    val messages: StateFlow<List<MessageModel>> = _messages.asStateFlow()

    private var messageListener: ValueEventListener? = null
    private var currentMessagesRef: com.google.firebase.database.DatabaseReference? = null
    private val database = FirebaseDatabase.getInstance("https://chattrix-9fbb6-default-rtdb.europe-west1.firebasedatabase.app")


    private var currentChatId: String = ""
    private var currentUserId: String = ""

    fun sendMessage(recieverId: String, message: String){
        chatService.sendMessage(message = message, recieverId = recieverId){
                success->
            //handling success or failure
        }
    }

    fun loadMessage(otherUserId: String){
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        currentUserId = currentUser.uid
        val chatId= chatService.getChatId(currentUser.uid, otherUserId)
        currentChatId = chatId

        messageListener?.let { listener ->
            currentMessagesRef?.removeEventListener(listener)
        }

        currentMessagesRef = database.getReference("chats/$chatId/messages")
        messageListener = currentMessagesRef!!.orderByChild("timestamp")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val messageList = mutableListOf<MessageModel>()
                    for(messageSnapshot in snapshot.children){
                        val message = messageSnapshot.getValue(MessageModel::class.java)
                        message?.let {
                            messageList.add(it)
                        }
                    }
                    _messages.value = messageList


                    markAllMessagesAsRead()
                }

                override fun onCancelled(error: DatabaseError) {
                    //handle error
                }
            })
    }


    private fun markAllMessagesAsRead() {
        if (currentChatId.isNotEmpty() && currentUserId.isNotEmpty()) {
            chatService.markAllMessagesAsRead(currentChatId, currentUserId)
        }
    }

    override fun onCleared() {
        super.onCleared()
        messageListener?.let { listener ->
            currentMessagesRef?.removeEventListener(listener)
        }
        messageListener = null
        currentMessagesRef = null
    }
}