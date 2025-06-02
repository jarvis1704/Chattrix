package com.biprangshu.chattrix.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.biprangshu.chattrix.data.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    application: Application
): AndroidViewModel(application) {

    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val db = FirebaseFirestore.getInstance()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _userList = MutableStateFlow<List<UserModel>>(mutableListOf())
    val userList: StateFlow<List<UserModel>> = _userList.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadUserList()
    }

    private fun loadUserList() {
        viewModelScope.launch(Dispatchers.IO) {
            // Add debug logging
            Log.d("MainViewModel", "Starting loadUserList")
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
                        Log.e("MainViewModel", "Error fetching users", error)
                        _errorMessage.value = "Failed to load users: ${error.message}"
                        _isLoading.value = false
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        Log.d("MainViewModel", "Snapshot received with ${snapshot.documents.size} documents")

                        val newList = mutableListOf<UserModel>()
                        for (document in snapshot.documents) {
                            Log.d("MainViewModel", "Processing document: ${document.id}")
                            Log.d("MainViewModel", "Document data: ${document.data}")

                            try {
                                val userModel = document.toObject(UserModel::class.java)
                                Log.d("MainViewModel", "Converted userModel: $userModel")

                                if (userModel?.userId != null && userModel.userId != currentUser.uid) {
                                    newList.add(userModel)
                                    Log.d("MainViewModel", "Added user: ${userModel.userName}")
                                } else if (userModel?.userId == currentUser.uid) {
                                    Log.d("MainViewModel", "Skipping current user: ${userModel.userName}")
                                } else {
                                    Log.w("MainViewModel", "UserModel is null or has null userId")
                                }
                            } catch (e: Exception) {
                                Log.e("MainViewModel", "Error converting document to UserModel", e)
                            }
                        }

                        Log.d("MainViewModel", "Final user list size: ${newList.size}")
                        _userList.value = newList
                        _isLoading.value = false

                        if (newList.isEmpty()) {
                            Log.w("MainViewModel", "No users found after filtering")
                        }
                    } else {
                        Log.w("MainViewModel", "Snapshot is null")
                        _isLoading.value = false
                    }
                }
        }
    }
}