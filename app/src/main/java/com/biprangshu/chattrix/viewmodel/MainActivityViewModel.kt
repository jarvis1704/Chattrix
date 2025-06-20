package com.biprangshu.chattrix.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.biprangshu.chattrix.data.MessageModel
import com.biprangshu.chattrix.data.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class UserChatInfo(
    val userModel: UserModel,
    val lastMessageText: String = "Tap to start chatting",
    val lastMessageTimeStamp: Long =0L,
    val isMessageSeen: Boolean,
)

data class UserCache(
    val users: Map<String, UserModel> = emptyMap(),
    val lastUpdated: Long = 0L
)

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    application: Application
): AndroidViewModel(application) {

    private val TAG = "MainActivityVM"
    private val CACHE_DURATION = 5 * 60 * 1000L // 5 minutes

    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val db = FirebaseFirestore.getInstance()
    private val realtimeDb = FirebaseDatabase.getInstance("https://chattrix-9fbb6-default-rtdb.europe-west1.firebasedatabase.app")

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _allUserList = MutableStateFlow<List<UserModel>>(emptyList())
    val allUserList: StateFlow<List<UserModel>> = _allUserList.asStateFlow()

    private val _userList = MutableStateFlow<List<UserChatInfo>>(emptyList())
    val userList: StateFlow<List<UserChatInfo>> = _userList.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Cache for user data
    private var userCache = UserCache()

    private var chatListener: ValueEventListener? = null
    private var allUsersListenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null


    init {
        Log.d(TAG, "ViewModel init. Current user: ${currentUser?.uid}")
        if (currentUser != null) {
            viewModelScope.launch {
                loadAllUsers()
                loadUsersWithChats()
            }
        } else {
            Log.w(TAG, "ViewModel init: Current user is null. Not loading data yet.")

        }
    }

    private fun loadAllUsers() {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d(TAG, "loadAllUsers: Starting. Current user: ${currentUser?.uid}")

            if(currentUser == null) {
                Log.e(TAG, "loadAllUsers: Current user is null. Cannot load all users.")
                _errorMessage.value = "User not authenticated"
                _allUserList.value = emptyList()
                return@launch
            }

            _isLoading.value = true
            _errorMessage.value = null

            // Remove previous listener if any
            allUsersListenerRegistration?.remove()

            allUsersListenerRegistration = db.collection("users")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "loadAllUsers: Error fetching all users", error)
                        _errorMessage.value = "Failed to load users: ${error.message}"
                        _isLoading.value = false
                        _allUserList.value = emptyList()
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        Log.d(TAG, "loadAllUsers: Snapshot received with ${snapshot.documents.size} documents.")
                        val newList = mutableListOf<UserModel>()
                        val cacheMap = mutableMapOf<String, UserModel>()
                        for (document in snapshot.documents) {
                            try {
                                val userModel = document.toObject(UserModel::class.java)
                                if (userModel?.userId != null) {
                                    cacheMap[userModel.userId] = userModel

                                    if (userModel.userId != currentUser.uid) {
                                        newList.add(userModel)
                                        Log.d(TAG, "loadAllUsers: Added user to all users: ${userModel.userName} (ID: ${userModel.userId})")
                                    } else {
                                        Log.d(TAG, "loadAllUsers: Skipping current user: ${userModel.userName}")
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "loadAllUsers: Error converting document to UserModel", e)
                            }
                        }


                        userCache = UserCache(cacheMap, System.currentTimeMillis())

                        Log.d(TAG, "loadAllUsers: Final all users list size: ${newList.size}")
                        _allUserList.value = newList
                    } else {
                        Log.d(TAG, "loadAllUsers: Snapshot is null.")
                        _allUserList.value = emptyList()
                    }
                    _isLoading.value = false
                }
        }
    }

    private fun loadUsersWithChats() {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d(TAG, "loadUsersWithChats: Starting.")
            if(currentUser == null) {
                Log.e(TAG, "loadUsersWithChats: Current user is null. Cannot load users with chats.")
                _userList.value = emptyList()
                return@launch
            }
            Log.d(TAG, "loadUsersWithChats: Current User ID: ${currentUser.uid}")

            chatListener?.let {
                realtimeDb.getReference("chats").removeEventListener(it)
                Log.d(TAG, "loadUsersWithChats: Removed previous chat listener.")
            }

            chatListener = realtimeDb.getReference("chats")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        Log.d(TAG, "loadUsersWithChats - onDataChange: Received chat data. Has children: ${snapshot.hasChildren()}")
                        if (!snapshot.hasChildren()) {
                            Log.d(TAG, "loadUsersWithChats - onDataChange: No chat nodes found.")
                            _userList.value = emptyList()
                            return
                        }

                        val userIdsWithChats = mutableSetOf<String>()
                        for (chatSnapshot in snapshot.children) {
                            val chatId = chatSnapshot.key
                            Log.d(TAG, "loadUsersWithChats - onDataChange: Processing chat ID: $chatId")

                            if (chatId == null) {
                                Log.w(TAG, "loadUsersWithChats - onDataChange: Null chatId found, skipping.")
                                continue
                            }

                            val userIds = chatId.split("-")
                            Log.d(TAG, "loadUsersWithChats - onDataChange: Split userIds from $chatId: $userIds")

                            if (userIds.size == 2) {
                                if (userIds[0] == currentUser.uid) {
                                    userIdsWithChats.add(userIds[1])
                                    Log.d(TAG, "loadUsersWithChats - onDataChange: Added ${userIds[1]} (from $chatId) to userIdsWithChats.")
                                } else if (userIds[1] == currentUser.uid) {
                                    userIdsWithChats.add(userIds[0])
                                    Log.d(TAG, "loadUsersWithChats - onDataChange: Added ${userIds[0]} (from $chatId) to userIdsWithChats.")
                                } else {
                                    Log.d(TAG, "loadUsersWithChats - onDataChange: Chat $chatId does not involve current user (${currentUser.uid}).")
                                }
                            } else {
                                Log.w(TAG, "loadUsersWithChats - onDataChange: ChatId $chatId did not split into 2 user IDs.")
                            }
                        }

                        Log.d(TAG, "loadUsersWithChats - onDataChange: Collected user IDs with chats: $userIdsWithChats (Count: ${userIdsWithChats.size})")

                        if (userIdsWithChats.isNotEmpty()) {
                            fetchChatPartnersDetails(userIdsWithChats.toList())
                        } else {
                            Log.d(TAG, "loadUsersWithChats - onDataChange: No relevant user IDs found. Setting active chat user list to empty.")
                            _userList.value = emptyList()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e(TAG, "loadUsersWithChats - onCancelled: Error loading chats from Realtime DB.", error.toException())
                        _errorMessage.value = "Failed to load chats: ${error.message}"
                        _userList.value = emptyList()
                    }
                })
            Log.d(TAG, "loadUsersWithChats: Chat listener attached to /chats.")
        }
    }


    fun getChatId(userId1: String, userId2: String): String {
        return if (userId1 < userId2){
            "$userId1-$userId2"
        }else{
            "$userId2-$userId1"
        }
    }



    private fun fetchChatPartnersDetails(partnerIds: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d(TAG, "fetchChatPartnersDetails: Starting for partner IDs: $partnerIds")
            if (partnerIds.isEmpty()) {
                Log.d(TAG, "fetchChatPartnersDetails: partnerIds list is empty. Setting _userList to empty.")
                _userList.value = emptyList()
                _isLoading.value = false
                return@launch
            }
            _isLoading.value = true
            try {

                val userModels = mutableListOf<UserModel>()
                val batches = partnerIds.chunked(10)

                for (batch in batches) {
                    Log.d(TAG, "fetchChatPartnersDetails: Querying Firestore for user batch: $batch")
                    try {
                        val querySnapshot = db.collection("users")
                            .whereIn("userId", batch)
                            .get()
                            .await()
                        Log.d(TAG, "fetchChatPartnersDetails: Firestore query for user batch returned ${querySnapshot.documents.size} documents.")
                        for (document in querySnapshot.documents) {
                            try {
                                val userModel = document.toObject(UserModel::class.java)
                                if (userModel != null) {
                                    userModels.add(userModel)
                                    Log.d(TAG, "fetchChatPartnersDetails: Successfully converted and added UserModel: ${userModel.userName} (ID: ${userModel.userId})")
                                } else {
                                    Log.w(TAG, "fetchChatPartnersDetails: UserModel conversion returned null for document ID: ${document.id}")
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "fetchChatPartnersDetails: Error converting Firestore document ${document.id} to UserModel", e)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "fetchChatPartnersDetails: Error fetching user batch $batch from Firestore.", e)
                    }
                }
                Log.d(TAG, "fetchChatPartnersDetails: Total fetched UserModel objects: ${userModels.size}. Users: ${userModels.joinToString { it.userName ?: "N/A" }}")

                if (userModels.isEmpty()) {
                    Log.w(TAG, "fetchChatPartnersDetails: No UserModel objects were successfully fetched or converted. Setting _userList to empty.")
                    _userList.value = emptyList()
                    _isLoading.value = false
                    return@launch
                }

                val userChatInfoList = mutableListOf<UserChatInfo>()
                val currentUid = currentUser?.uid ?: run {
                    Log.e(TAG, "fetchChatPartnersDetails: Current user UID is null. Cannot proceed to fetch last messages.")
                    _userList.value = emptyList()
                    _isLoading.value = false
                    return@launch
                }

                for (userModel in userModels) {
                    val partnerUid = userModel.userId ?: continue
                    val chatId = getChatId(currentUid, partnerUid)
                    Log.d(TAG, "fetchChatPartnersDetails: Processing user ${userModel.userName} (partnerUid: $partnerUid), generating chatId: $chatId")
                    var lastMsgText = "Tap to start chatting"
                    var lastMsgTimestamp = 0L
                    var hasUnreadMessages = false // FIXED: Changed variable name for clarity

                    try {
                        Log.d(TAG, "fetchChatPartnersDetails: Attempting to fetch last message for chat $chatId")
                        val lastMessageSnapshot = realtimeDb.getReference("chats/$chatId/messages")
                            .orderByChild("timestamp")
                            .limitToLast(1)
                            .get().await()

                        if (lastMessageSnapshot.hasChildren()) {
                            Log.d(TAG, "fetchChatPartnersDetails: Last message snapshot for $chatId HAS children.")
                            val messageDataSnapshot = lastMessageSnapshot.children.first()
                            val messageData = messageDataSnapshot.getValue(MessageModel::class.java)
                            if (messageData != null) {
                                val senderPrefix = if (messageData.senderId == currentUid) "You: " else ""
                                lastMsgText = senderPrefix + (messageData.message ?: "")
                                lastMsgTimestamp = messageData.timestamp ?: 0L


                                hasUnreadMessages = messageData.senderId != currentUid && messageData.isRead == false

                                Log.d(TAG, "fetchChatPartnersDetails: Last message for $chatId: '$lastMsgText' (ts: $lastMsgTimestamp), hasUnread: $hasUnreadMessages")
                            } else {
                                Log.w(TAG, "fetchChatPartnersDetails: Failed to convert last message snapshot to MessageModel for $chatId. Snapshot value: ${messageDataSnapshot.value}")
                            }
                        } else {
                            Log.d(TAG, "fetchChatPartnersDetails: No last message found for chat $chatId (snapshot has no children).")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "fetchChatPartnersDetails: Error fetching last message for chat $chatId", e)
                    }


                    userChatInfoList.add(UserChatInfo(userModel, lastMsgText, lastMsgTimestamp, !hasUnreadMessages))
                }

                Log.d(TAG, "fetchChatPartnersDetails: Constructed userChatInfoList with ${userChatInfoList.size} items before sorting: ${userChatInfoList.joinToString {it.userModel.userName ?: "N/A" }}")
                _userList.value = userChatInfoList.sortedByDescending { it.lastMessageTimeStamp }

            } catch (e: Exception) {
                Log.e(TAG, "fetchChatPartnersDetails: CRITICAL ERROR in main try-catch block.", e)
                _errorMessage.value = "Failed to load chat users: ${e.message}"
                _userList.value = emptyList()
            } finally {
                _isLoading.value = false
                Log.d(TAG, "fetchChatPartnersDetails: Finished. _isLoading is now false. Final _userList size: ${_userList.value.size}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "onCleared: Removing listeners.")
        chatListener?.let {
            realtimeDb.getReference("chats").removeEventListener(it)
        }
        allUsersListenerRegistration?.remove()
    }
}