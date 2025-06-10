package com.biprangshu.chattrix.authentication

import android.app.Application
import android.content.Intent
import android.content.IntentSender
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.biprangshu.chattrix.data.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val googleAuthClient: GoogleAuthClient,
    application: Application
) : AndroidViewModel(application) {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Initial)
    val updateState: StateFlow<UpdateState> = _updateState

    private val credentialManager = CredentialManager.create(application)

    // Add a flag to prevent multiple simultaneous Google Sign-In attempts
    private var isGoogleSignInInProgress = false

    init {
        checkAuthState()
    }

    fun loginWithEmail(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email and Password cannot be empty")
            return
        }

        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.SignedIn(auth.currentUser)
                } else {
                    val errorMessage = task.exception?.message ?: "Login failed. Please check your credentials."
                    _authState.value = AuthState.Error(errorMessage)
                }
            }
    }

    fun signupWithEmail(email: String, password: String, displayName: String) {
        if (email.isEmpty() || password.isEmpty() || displayName.isEmpty()) {
            _authState.value = AuthState.Error("All fields must be filled")
            return
        }

        _authState.value = AuthState.Loading

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(displayName)
                        .build()

                    user?.updateProfile(profileUpdates)?.addOnCompleteListener { profileUpdateTask ->
                        if(profileUpdateTask.isSuccessful) {
                            user.let { saveUserToFirestore(it, displayName) }
                            _authState.value = AuthState.SignedIn(user)
                        } else {
                            _authState.value = AuthState.SignedIn(user)
                            Log.e("AuthViewModel", "User profile update failed", profileUpdateTask.exception)
                        }
                    }
                } else {
                    val exception = task.exception
                    val errorMessage = when (exception) {
                        is FirebaseAuthUserCollisionException -> "An account already exists with this email address."
                        else -> exception?.message ?: "Signup failed. Please try again."
                    }
                    _authState.value = AuthState.Error(errorMessage)
                }
            }
    }

    suspend fun signInWithGoogle(): IntentSender? {
        return try {
            _authState.value = AuthState.Loading
            Log.d("AuthViewModel", "Started Google Login process")
            val intentSender = googleAuthClient.signIn()

            if(intentSender == null){
                _authState.value = AuthState.Error("Failed to Log in, could not initiate google sign in")
            }

            intentSender
        } catch (e: Exception){
            Log.e("AuthViewModel", "Google sign-in error", e)
            _authState.value = AuthState.Error(e.message ?: "Unknown error occurred")
            null
        }
    }

    fun handleGoogleSignInResult(intent: Intent){
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading

                val result = googleAuthClient.getSignInResultFromIntent(intent)

                if (result.data != null){
                    val currentUser = auth.currentUser
                    _authState.value = AuthState.SignedIn(currentUser)

                    currentUser?.let {
                        saveUserToFirestore(it, it.displayName?: "User")
                    }
                }else{
                    _authState.value = AuthState.Error(result.errorMessage ?: "Sign in failed")
                }
            }catch (e: Exception){
                Log.e("AuthViewModel", "Error handling Google sign-in result", e)
                _authState.value = AuthState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun checkAuthState() {
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
        viewModelScope.launch {
            try {
                auth.signOut()
                _authState.value = AuthState.SignedOut
                credentialManager.clearCredentialState(androidx.credentials.ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error clearing credential state during sign out", e)
            }
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
            .set(userModel, SetOptions.merge())
            .addOnSuccessListener {
                Log.d("AuthViewModel", "User saved/updated in Firestore successfully")
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
                            Log.d("AuthViewModel", "Profile updated successfully in Auth and Firestore")
                            _updateState.value = UpdateState.Success
                            _authState.value = AuthState.SignedIn(auth.currentUser)
                        }
                        .addOnFailureListener { e ->
                            Log.e("AuthViewModel", "Failed to update Firestore", e)
                            _updateState.value =
                                UpdateState.Error("Failed to update profile database: ${e.message}")
                        }
                } else {
                    _updateState.value =
                        UpdateState.Error("Failed to update profile: ${profileTask.exception?.message}")
                }
            }
    }

    fun resetUpdateState() {
        _updateState.value = UpdateState.Initial
    }

    // Add method to reset auth state if needed
    fun resetAuthState() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.SignedOut
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

sealed class UpdateState {
    object Initial : UpdateState()
    object Loading : UpdateState()
    object Success : UpdateState()
    data class Error(val message: String) : UpdateState()
}

data class SignInResult(
    val data: UserData?,
    val errorMessage: String?
)

data class UserData(
    val userId: String,
    val username: String?
)