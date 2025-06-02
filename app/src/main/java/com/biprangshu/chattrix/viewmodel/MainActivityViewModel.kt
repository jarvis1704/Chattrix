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

    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val db = FirebaseFirestore.getInstance()
    private val realtimeDb = FirebaseDatabase.getInstance("https://chattrix-9fbb6-default-rtdb.europe-west1.firebasedatabase.app")

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // All users (for NewChatScreen)
    private val _allUserList = MutableStateFlow<List<UserModel>>(mutableListOf())
    val allUserList: StateFlow<List<UserModel>> = _allUserList.asStateFlow()

    // Users with active chats (for HomeScreen)
    private val _userList = MutableStateFlow<List<UserModel>>(mutableListOf())
    val userList: StateFlow<List<UserModel>> = _userList.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var chatListener: ValueEventListener? = null

    init {
        loadAllUsers()
        loadUsersWithChats()
    }

    // Load all users except current user (for NewChatScreen)
    private fun loadAllUsers() {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("MainViewModel", "Starting loadAllUsers")
            Log.d("MainViewModel", "Current user: ${currentUser?.uid}")

            if(currentUser == null) {
                Log.e("MainViewModel", "Current user is null")
                _errorMessage.value = "User not authenticated"
                return@launch
            }

            _isLoading.value = true
            _errorMessage.value = null

            db.collection("users")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("MainViewModel", "Error fetching all users", error)
                        _errorMessage.value = "Failed to load users: ${error.message}"
                        _isLoading.value = false
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        Log.d("MainViewModel", "All users snapshot received with ${snapshot.documents.size} documents")

                        val newList = mutableListOf<UserModel>()
                        for (document in snapshot.documents) {
                            try {
                                val userModel = document.toObject(UserModel::class.java)
                                if (userModel?.userId != null && userModel.userId != currentUser.uid) {
                                    newList.add(userModel)
                                    Log.d("MainViewModel", "Added user to all users: ${userModel.userName}")
                                }
                            } catch (e: Exception) {
                                Log.e("MainViewModel", "Error converting document to UserModel", e)
                            }
                        }

                        Log.d("MainViewModel", "Final all users list size: ${newList.size}")
                        _allUserList.value = newList
                        _isLoading.value = false
                    }
                }
        }
    }

    // Load users with whom current user has active chats (for HomeScreen)
    private fun loadUsersWithChats() {
        viewModelScope.launch(Dispatchers.IO) {
            if(currentUser == null) {
                Log.e("MainViewModel", "Current user is null for chat loading")
                return@launch
            }

            // Remove previous listener if exists
            chatListener?.let {
                realtimeDb.getReference("chats").removeEventListener(it)
            }

            // Listen to all chats where current user is involved
            chatListener = realtimeDb.getReference("chats")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        Log.d("MainViewModel", "Chat data changed, processing...")

                        val userIdsWithChats = mutableSetOf<String>()

                        // Extract user IDs from chat IDs
                        for (chatSnapshot in snapshot.children) {
                            val chatId = chatSnapshot.key ?: continue
                            Log.d("MainViewModel", "Processing chat: $chatId")

                            // Chat ID format: "userId1_userId2" (sorted alphabetically)
                            val userIds = chatId.split("_")
                            if (userIds.size == 2) {
                                if (userIds[0] == currentUser.uid) {
                                    userIdsWithChats.add(userIds[1])
                                } else if (userIds[1] == currentUser.uid) {
                                    userIdsWithChats.add(userIds[0])
                                }
                            }
                        }

                        Log.d("MainViewModel", "Found ${userIdsWithChats.size} users with chats")

                        // Fetch user details for these user IDs
                        if (userIdsWithChats.isNotEmpty()) {
                            fetchUsersById(userIdsWithChats.toList())
                        } else {
                            _userList.value = emptyList()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("MainViewModel", "Error loading chats", error.toException())
                        _errorMessage.value = "Failed to load chats: ${error.message}"
                    }
                })
        }
    }

    private fun fetchUsersById(userIds: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val users = mutableListOf<UserModel>()


                val batches = userIds.chunked(10)

                for (batch in batches) {
                    try {
                        val querySnapshot = db.collection("users")
                            .whereIn("userId", batch)
                            .get()
                            .await()

                        for (document in querySnapshot.documents) {
                            try {
                                val userModel = document.toObject(UserModel::class.java)
                                if (userModel?.userId != null) {
                                    users.add(userModel)
                                    Log.d("MainViewModel", "Added user with chat: ${userModel.userName}")
                                }
                            } catch (e: Exception) {
                                Log.e("MainViewModel", "Error converting chat user document", e)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("MainViewModel", "Error fetching batch of users", e)
                    }
                }

                // Update the state with users who have chats
                val distinctUsers = users.distinctBy { it.userId }
                _userList.value = distinctUsers
                Log.d("MainViewModel", "Updated chat users list size: ${distinctUsers.size}")

            } catch (e: Exception) {
                Log.e("MainViewModel", "Error in fetchUsersById", e)
                _errorMessage.value = "Failed to load chat users: ${e.message}"
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        chatListener?.let {
            realtimeDb.getReference("chats").removeEventListener(it)
        }
    }
}