package com.biprangshu.chattrix.viewmodel

import android.app.Application
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
): AndroidViewModel(application){

    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val databaseReference= FirebaseDatabase.getInstance()


    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _userList = MutableStateFlow(mutableListOf<UserModel>())
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


            databaseReference.getReference("users").addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    val newList = mutableListOf<UserModel>()

                    for (userSnapshot in snapshot.children) {
                        val userModel = userSnapshot.getValue(UserModel::class.java)
                        userModel?.let {
                            newList.add(it)
                        }
                    }

                    _userList.value = newList  // Assign the new list to the StateFlow
                    _isLoading.value = false
                }

                override fun onCancelled(error: DatabaseError) {
                    _isLoading.value = false
                }
            })
        }
    }

}