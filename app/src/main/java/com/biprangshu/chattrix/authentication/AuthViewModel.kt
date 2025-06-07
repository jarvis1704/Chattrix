package com.biprangshu.chattrix.authentication

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.biprangshu.chattrix.R
import com.biprangshu.chattrix.data.UserModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
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

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Initial)
    val updateState: StateFlow<UpdateState> = _updateState

    private val credentialManager = CredentialManager.create(application)

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
                    _authState.value = AuthState.Error(task.exception?.message ?: "Login failed")
                }
            }
    }

    fun signupWithEmail(email: String, password: String, displayName: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email and Password cannot be empty")
            return
        }

        _authState.value = AuthState.Loading

        try {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser

                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(displayName)
                            .build()

                        user?.updateProfile(profileUpdates)?.addOnCompleteListener {
                            user.let { saveUserToFirestore(it, displayName) }
                        }
                        _authState.value = AuthState.SignedIn(user)
                    } else {
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

    fun signInWithGoogle(context: Context) {
        _authState.value = AuthState.Loading

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getApplication<Application>().getString(R.string.default_web_client_id))
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        viewModelScope.launch {
            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = context
                )
                handleGoogleSignInResult(result)
            } catch (e: GetCredentialException) {
                Log.e("AuthViewModel", "Google Sign-In failed", e)

                when (e) {
                    is NoCredentialException -> {

                        retryGoogleSignInWithFilteredAccounts(context)
                    }
                    else -> {
                        val errorMessage = when {
                            e.message?.contains("canceled") == true -> "Sign-in was canceled"
                            e.message?.contains("network") == true -> "Network error. Please check your connection"
                            else -> "Google sign-in failed: ${e.message}"
                        }
                        _authState.value = AuthState.Error(errorMessage)
                    }
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Unexpected error during Google Sign-In", e)
                _authState.value = AuthState.Error("Unexpected error: ${e.message}")
            }
        }
    }

    private fun retryGoogleSignInWithFilteredAccounts(context: Context) {
        Log.d("AuthViewModel", "Retrying Google Sign-In with filtered accounts")

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true) // Try with filtered accounts as fallback
            .setServerClientId(getApplication<Application>().getString(R.string.default_web_client_id))
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        viewModelScope.launch {
            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = context
                )
                handleGoogleSignInResult(result)
            } catch (e: GetCredentialException) {
                Log.e("AuthViewModel", "Google Sign-In retry failed", e)
                val errorMessage = when {
                    e.message?.contains("No credentials available") == true ->
                        "No Google accounts found. Please add a Google account to your device and try again."
                    e.message?.contains("canceled") == true -> "Sign-in was canceled"
                    else -> "Google sign-in failed: ${e.message}"
                }
                _authState.value = AuthState.Error(errorMessage)
            }
        }
    }

    private fun handleGoogleSignInResult(result: GetCredentialResponse) {
        when (val credential = result.credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        val idToken = googleIdTokenCredential.idToken

                        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                        auth.signInWithCredential(firebaseCredential)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val user = auth.currentUser
                                    user?.let {
                                        saveUserToFirestore(it, user.displayName ?: "User")
                                    }
                                    _authState.value = AuthState.SignedIn(user)
                                } else {
                                    _authState.value = AuthState.Error(task.exception?.message ?: "Firebase authentication failed")
                                }
                            }
                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e("AuthViewModel", "Invalid Google ID token response", e)
                        _authState.value = AuthState.Error("Invalid Google ID token")
                    }
                } else {
                    Log.e("AuthViewModel", "Unexpected credential type: ${credential.type}")
                    _authState.value = AuthState.Error("Unexpected credential type")
                }
            }
            else -> {
                Log.e("AuthViewModel", "Unexpected credential type")
                _authState.value = AuthState.Error("Unexpected credential type")
            }
        }
    }

    fun checkAuthState() {
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
        viewModelScope.launch {
            try {
                credentialManager.clearCredentialState(
                    androidx.credentials.ClearCredentialStateRequest()
                )
                auth.signOut()
                _authState.value = AuthState.SignedOut
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error during sign out", e)
                auth.signOut()
                _authState.value = AuthState.SignedOut
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
                            _authState.value = AuthState.SignedIn(auth.currentUser)
                        }
                        .addOnFailureListener { e ->
                            Log.e("AuthViewModel", "Failed to update Firestore", e)
                            _updateState.value =
                                UpdateState.Error("Failed to update profile: ${e.message}")
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