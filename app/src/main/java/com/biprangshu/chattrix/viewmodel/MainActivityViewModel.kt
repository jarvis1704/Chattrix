package com.biprangshu.chattrix.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.biprangshu.chattrix.data.UserModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
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

    init {
        loadUserList()
    }

    private fun loadUserList() {
        viewModelScope.launch(Dispatchers.IO) {
            if(currentUser == null) {
                return@launch
            }

            _isLoading.value = true

            // Listen for realtime updates
            db.collection("users")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("MainViewModel", "Error fetching users", error)
                        _isLoading.value = false
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val newList = mutableListOf<UserModel>()
                        for (document in snapshot.documents) {
                            val userModel = document.toObject(UserModel::class.java)
                            if (userModel?.userId != currentUser.uid) {
                                userModel?.let {
                                    newList.add(it)
                                }
                            }
                        }
                        _userList.value = newList
                        _isLoading.value = false
                    }
                }
        }
    }
}
