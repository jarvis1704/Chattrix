package com.biprangshu.chattrix.authentication

import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.biprangshu.chattrix.R
import com.biprangshu.chattrix.data.UserModel
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth = Firebase.auth,
    application: Application
) : AndroidViewModel(application) {

    constructor(application: Application) : this(Firebase.auth, application)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState

    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(application.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        GoogleSignIn.getClient(application, gso)
    }

    init {
        // Check auth state when ViewModel is created
        checkAuthState()
    }

    fun loginWithEmail(email: String, password: String){

        if(email.isEmpty() || password.isEmpty()){
            _authState.value = AuthState.Error("Email and Password cannot be empty")
            return
        }


        _authState.value= AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                task->
                if (task.isSuccessful){
                    _authState.value = AuthState.SignedIn(auth.currentUser)
                }else{
                    _authState.value = AuthState.Error(task.exception?.message ?: "Login failed")
                }
            }
    }

    fun signupWithEmail(email: String, password: String, displayName: String) {
        if(email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email and Password cannot be empty")
            return
        }

        _authState.value = AuthState.Loading

        try {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
//                        user?.let { saveUserToDatabase(it) }

                        val profileUpdates = UserProfileChangeRequest.Builder().setDisplayName(displayName).build()

                        user?.updateProfile(profileUpdates)?.addOnCompleteListener {
                            saveUserToDatabase(user!!,displayName)
                        }
                        _authState.value = AuthState.SignedIn(user)
                    } else {
                        // Check specifically for network errors
                        val exception = task.exception
                        val errorMessage = when {
                            exception is java.net.UnknownHostException ||
                                    exception is java.net.SocketTimeoutException ||
                                    exception?.message?.contains("network") == true ->
                                "Network error: Please check your internet connection"
                            else -> exception?.message ?: "Signup failed"
                        }
                        _authState.value = AuthState.Error(errorMessage)
                    }
                }
        } catch (e: Exception) {
            _authState.value = AuthState.Error("Unexpected error: ${e.message}")
        }
    }

//    fun uploadUserName(username: String, displayName: String) {
//        val currentUser = auth.currentUser
//        if (currentUser != null) {
//            _authState.value = AuthState.Loading
//
//            val database = FirebaseDatabase.getInstance("https://chattrix-9fbb6-default-rtdb.europe-west1.firebasedatabase.app")
//            val userRef = database.getReference("users").child(currentUser.uid)
//
//            userRef.child("userName").setValue(username)
//                .addOnSuccessListener {
//                    Log.d("AuthViewModel", "Username updated successfully")
//                    _authState.value = AuthState.SignedIn(currentUser)
//                }
//                .addOnFailureListener { e ->
//                    Log.e("AuthViewModel", "Failed to update username", e)
//                    _authState.value = AuthState.Error("Failed to update username: ${e.message}")
//                }
//        } else {
//            _authState.value = AuthState.Error("No user is currently signed in")
//        }
//    }

    fun getSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }

    fun signInWithGoogle(idToken: String) {
        _authState.value = AuthState.Loading

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success
                    val user = auth.currentUser

                    _authState.value = AuthState.SignedIn(user)
                } else {
                    // Sign in fails
                    _authState.value = AuthState.Error(task.exception?.message ?: "Google sign-in failed")
                }
            }

    }

    fun checkAuthState() {
        // Start with loading state while checking
        _authState.value = AuthState.Loading

        viewModelScope.launch {
            val currentUser = auth.currentUser
            _authState.value = if (currentUser != null) {
                AuthState.SignedIn(currentUser)
            } else {
                AuthState.SignedOut
            }
        }
    }

    fun signOut() {
        auth.signOut()
        googleSignInClient.signOut().addOnCompleteListener {
            _authState.value = AuthState.SignedOut
        }
    }

    private fun saveUserToDatabase(user: FirebaseUser, displayName: String) {
        val db = FirebaseFirestore.getInstance()

        val userModel = UserModel(
            userId = user.uid,
            userName = displayName.ifEmpty { user.displayName?: "User" },
            profileImage = user.photoUrl?.toString(),
            mobileNumber = user.phoneNumber
        )
        Log.d("AuthViewModel", "Saving user to Firestore: $userModel")
        db.collection("users").document(user.uid)
            .set(userModel)
            .addOnSuccessListener {
                Log.d("AuthViewModel", "User saved to Firestore successfully")
            }
            .addOnFailureListener { e ->
                Log.e("AuthViewModel", "Failed to save user to Firestore", e)
            }
    }


}

sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    object SignedOut : AuthState()
    data class SignedIn(val user: FirebaseUser?) : AuthState()
    data class Error(val message: String) : AuthState()
}