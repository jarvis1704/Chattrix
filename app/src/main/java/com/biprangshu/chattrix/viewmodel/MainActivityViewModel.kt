package com.biprangshu.chattrix.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.biprangshu.chattrix.data.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    application: Application
): AndroidViewModel(application) {

    private val TAG = "MainActivityVM" // For logging

    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val db = FirebaseFirestore.getInstance()
    private val realtimeDb = FirebaseDatabase.getInstance("https.://chattrix-9fbb6-default-rtdb.europe-west1.firebasedatabase.app")

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _allUserList = MutableStateFlow<List<UserModel>>(emptyList()) // Initialize with emptyList
    val allUserList: StateFlow<List<UserModel>> = _allUserList.asStateFlow()

    private val _userList = MutableStateFlow<List<UserModel>>(emptyList()) // Initialize with emptyList
    val userList: StateFlow<List<UserModel>> = _userList.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var chatListener: ValueEventListener? = null
    private var allUsersListenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null


    init {
        Log.d(TAG, "ViewModel init. Current user: ${currentUser?.uid}")
        if (currentUser != null) {
            loadAllUsers()
            loadUsersWithChats()
        } else {
            Log.w(TAG, "ViewModel init: Current user is null. Not loading data yet.")
            // Consider a mechanism to retry loading if auth state changes later
            // or ensure this ViewModel is only created post-authentication.
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
                        for (document in snapshot.documents) {
                            try {
                                val userModel = document.toObject(UserModel::class.java)
                                if (userModel?.userId != null && userModel.userId != currentUser.uid) {
                                    newList.add(userModel)
                                    Log.d(TAG, "loadAllUsers: Added user to all users: ${userModel.userName} (ID: ${userModel.userId})")
                                } else if (userModel?.userId == currentUser.uid) {
                                    Log.d(TAG, "loadAllUsers: Skipping current user: ${userModel.userName}")
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "loadAllUsers: Error converting document to UserModel", e)
                            }
                        }
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
                _userList.value = emptyList() // Clear list if user is null
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
                            fetchUsersById(userIdsWithChats.toList())
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

    private fun fetchUsersById(userIds: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d(TAG, "fetchUsersById: Starting for user IDs: $userIds")
            if (userIds.isEmpty()) {
                Log.d(TAG, "fetchUsersById: userIds list is empty. Not fetching from Firestore.")
                _userList.value = emptyList() // Should already be handled by caller, but defensive
                return@launch
            }
            _isLoading.value = true
            try {
                val users = mutableListOf<UserModel>()
                val batches = userIds.chunked(10) // Max 10 for 'in' query

                for (batch in batches) {
                    Log.d(TAG, "fetchUsersById: Querying Firestore for batch: $batch")
                    try {
                        val querySnapshot = db.collection("users")
                            .whereIn("userId", batch) // Make sure 'userId' is the exact field name in Firestore
                            .get()
                            .await()

                        Log.d(TAG, "fetchUsersById: Firestore query for batch returned ${querySnapshot.documents.size} documents.")

                        for (document in querySnapshot.documents) {
                            try {
                                val userModel = document.toObject(UserModel::class.java)
                                if (userModel?.userId != null) {
                                    users.add(userModel)
                                    Log.d(TAG, "fetchUsersById: Successfully fetched and added user: ${userModel.userName} (ID: ${userModel.userId})")
                                } else {
                                    Log.w(TAG, "fetchUsersById: Converted UserModel is null or has null userId. Document ID: ${document.id}")
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "fetchUsersById: Error converting Firestore document ${document.id} to UserModel.", e)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "fetchUsersById: Error fetching batch $batch from Firestore.", e)
                    }
                }

                val distinctUsers = users.distinctBy { it.userId }
                _userList.value = distinctUsers
                Log.d(TAG, "fetchUsersById: Successfully updated _userList with ${distinctUsers.size} users: ${distinctUsers.joinToString { it.userName ?: "N/A" }}")

            } catch (e: Exception) {
                Log.e(TAG, "fetchUsersById: General error.", e)
                _errorMessage.value = "Failed to load chat user details: ${e.message}"
                _userList.value = emptyList()
            } finally {
                _isLoading.value = false
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