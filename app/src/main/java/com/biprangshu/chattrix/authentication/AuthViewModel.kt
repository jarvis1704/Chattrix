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
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth,
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

    fun signInWithGoogle(context: Context) {
        // Prevent multiple simultaneous attempts
        if (isGoogleSignInInProgress) {
            Log.d("AuthViewModel", "Google Sign-In already in progress, ignoring request")
            return
        }

        isGoogleSignInInProgress = true
        Log.d("AuthViewModel", "Starting Google Sign-In process")

        viewModelScope.launch {
            try {
                // Clear any previous credential state first
                try {
                    credentialManager.clearCredentialState(androidx.credentials.ClearCredentialStateRequest())
                    // Small delay to ensure clearing is complete
                    delay(100)
                } catch (e: Exception) {
                    Log.w("AuthViewModel", "Could not clear credential state", e)
                }

                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false) // Always show account picker
                    .setServerClientId(getApplication<Application>().getString(R.string.default_web_client_id))
                    .setAutoSelectEnabled(false) // Disable auto-select to ensure popup appears
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                Log.d("AuthViewModel", "Attempting to get Google credential")
                val result = credentialManager.getCredential(context = context, request)
                handleGoogleSignInResult(result)

            } catch (e: GetCredentialException) {
                Log.e("AuthViewModel", "Google Sign-In GetCredentialException: ${e.javaClass.simpleName}", e)

                val errorMessage = when (e) {
                    is NoCredentialException -> {
                        "No Google accounts found. Please add a Google account to your device."
                    }
                    else -> {
                        val message = e.message ?: ""
                        when {
                            message.contains("canceled", ignoreCase = true) -> {
                                "Google Sign-In was canceled."
                            }
                            message.contains("network", ignoreCase = true) -> {
                                "Network error. Please check your internet connection and try again."
                            }
                            message.contains("developer", ignoreCase = true) -> {
                                "Google Sign-In configuration error. Please contact support."
                            }
                            else -> {
                                "Google Sign-In failed. Please try again."
                            }
                        }
                    }
                }

                _authState.value = AuthState.Error(errorMessage)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Unexpected error during Google Sign-In", e)
                _authState.value = AuthState.Error("An unexpected error occurred. Please try again.")
            } finally {
                isGoogleSignInInProgress = false
            }
        }
    }

    private fun handleGoogleSignInResult(result: GetCredentialResponse) {
        Log.d("AuthViewModel", "Handling Google Sign-In result")

        when (val credential = result.credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        val idToken = googleIdTokenCredential.idToken

                        Log.d("AuthViewModel", "Successfully parsed Google ID token")

                        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                        auth.signInWithCredential(firebaseCredential)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val user = auth.currentUser
                                    Log.d("AuthViewModel", "Firebase authentication successful for user: ${user?.email}")

                                    user?.let {
                                        saveUserToFirestore(it, it.displayName ?: "User")
                                    }
                                    _authState.value = AuthState.SignedIn(user)
                                } else {
                                    Log.e("AuthViewModel", "Firebase authentication failed", task.exception)
                                    val errorMessage = task.exception?.message?.let { message ->
                                        when {
                                            message.contains("network", ignoreCase = true) ->
                                                "Network error during sign-in. Please check your connection."
                                            message.contains("invalid", ignoreCase = true) ->
                                                "Invalid Google account. Please try with a different account."
                                            else -> "Authentication failed: $message"
                                        }
                                    } ?: "Firebase authentication failed. Please try again."

                                    _authState.value = AuthState.Error(errorMessage)
                                }
                            }
                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e("AuthViewModel", "Failed to parse Google ID token", e)
                        _authState.value = AuthState.Error("Failed to process Google Sign-In response. Please try again.")
                    } catch (e: Exception) {
                        Log.e("AuthViewModel", "Unexpected error processing Google credential", e)
                        _authState.value = AuthState.Error("Failed to process Google Sign-In. Please try again.")
                    }
                } else {
                    Log.e("AuthViewModel", "Unexpected credential type: ${credential.type}")
                    _authState.value = AuthState.Error("Invalid credential type received from Google.")
                }
            }
            else -> {
                Log.e("AuthViewModel", "Unexpected credential class: ${credential?.javaClass?.simpleName}")
                _authState.value = AuthState.Error("Invalid credential format received.")
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