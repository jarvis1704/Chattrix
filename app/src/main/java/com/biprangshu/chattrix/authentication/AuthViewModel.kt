package com.biprangshu.chattrix.authentication

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.biprangshu.chattrix.R
import com.biprangshu.chattrix.data.UserModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
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

    // Add a separate state for profile updates
    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Initial)
    val updateState: StateFlow<UpdateState> = _updateState

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

        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful){
                    _authState.value = AuthState.SignedIn(auth.currentUser)
                } else {
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

                        val profileUpdates = UserProfileChangeRequest.Builder().setDisplayName(displayName).build()

                        user?.updateProfile(profileUpdates)?.addOnCompleteListener {
                            user.let { saveUserToFirestore(it, displayName) }
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
                    user?.let {
                        saveUserToFirestore(it, user.displayName ?: "User")
                    }
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

    private fun saveUserToFirestore(user: FirebaseUser, displayName: String) {
        val db = FirebaseFirestore.getInstance()

        val userModel = UserModel(
            userId = user.uid,
            userName = displayName.ifEmpty { user.displayName ?: "User" },
            profileImage = user.photoUrl?.toString(),
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

    fun updateUserName(displayName: String) {
        val user = auth.currentUser
        if (user == null) {
            _updateState.value = UpdateState.Error("No user signed in")
            return
        }

        _updateState.value = UpdateState.Loading


        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(displayName)
            .build()

        user.updateProfile(profileUpdates)
            .addOnCompleteListener { profileTask ->
                if (profileTask.isSuccessful) {
                    val db = FirebaseFirestore.getInstance()
                    db.collection("users").document(user.uid)
                        .update("userName", displayName)
                        .addOnSuccessListener {
                            Log.d("AuthViewModel", "Profile updated successfully")
                            _updateState.value = UpdateState.Success
                            // Force refresh the auth state to trigger UI update
                            _authState.value = AuthState.SignedIn(auth.currentUser)
                        }
                        .addOnFailureListener { e ->
                            Log.e("AuthViewModel", "Failed to update Firestore", e)
                            _updateState.value = UpdateState.Error("Failed to update profile: ${e.message}")
                        }
                } else {
                    _updateState.value = UpdateState.Error("Failed to update profile: ${profileTask.exception?.message}")
                }
            }
    }

    fun resetUpdateState() {
        _updateState.value = UpdateState.Initial
    }
}

sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    object SignedOut : AuthState()
    data class SignedIn(val user: FirebaseUser?) : AuthState()
    data class Error(val message: String) : AuthState()
}

sealed class UpdateState {
    object Initial : UpdateState()
    object Loading : UpdateState()
    object Success : UpdateState()
    data class Error(val message: String) : UpdateState()
}