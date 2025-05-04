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
    private val database = FirebaseDatabase.getInstance("https://chattrix-9fbb6-default-rtdb.europe-west1.firebasedatabase.app")

    fun sendMessage(recieverId: String, message: String){
        chatService.sendMessage(message = message, recieverId = recieverId){
            success->
            //handling success or failure
        }
    }

    fun loadMessage(otherUserId: String){
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        val chatId= chatService.getChatId(currentUser.uid, otherUserId)

        //remove duplicate message listener
        messageListener?.let {
            database.getReference("chats/$chatId/messages").removeEventListener(it)
        }

        //set up real time messaging
        messageListener?.let {
            database.getReference("chats/$chatId/messages").orderByChild("timestamp")
                .addValueEventListener(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val messaageList = mutableListOf<MessageModel>()
                        for(messageSnapshot in snapshot.children){
                            val message = messageSnapshot.getValue(MessageModel::class.java)
                        }
                        _messages.value=messaageList
                    }

                    override fun onCancelled(error: DatabaseError) {
                        //handle error
                    }
                })
        }
    }

    override fun onCleared() {
        super.onCleared()
        messageListener?.let {
            database.getReference("chats").removeEventListener(it)
        }
    }

}