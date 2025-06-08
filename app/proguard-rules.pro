# Add this project-specific ProGuard rules file to your app-level proguard-rules.pro file

# Keep Firebase Realtime Database classes
-keep class com.google.firebase.database.** { *; }
-keep class com.google.firebase.firestore.** { *; }
-keep class com.google.firebase.auth.** { *; }

# Keep your data models - CRITICAL for Firebase serialization
-keep class com.biprangshu.chattrix.data.** { *; }

# Keep model classes used with Firebase
-keep class com.biprangshu.chattrix.data.MessageModel { *; }
-keep class com.biprangshu.chattrix.data.UserModel { *; }
-keep class com.biprangshu.chattrix.viewmodel.UserChatInfo { *; }

# Keep Firebase serialization annotations
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# Keep classes that use Firebase serialization
-keepclassmembers class * {
    @com.google.firebase.database.PropertyName <fields>;
    @com.google.firebase.database.PropertyName <methods>;
    @com.google.firebase.firestore.PropertyName <fields>;
    @com.google.firebase.firestore.PropertyName <methods>;
}

# Keep default constructors for Firebase model classes
-keepclassmembers class com.biprangshu.chattrix.data.** {
    <init>();
}

# Keep getter and setter methods for Firebase models
-keepclassmembers class com.biprangshu.chattrix.data.** {
    public <methods>;
    public <fields>;
}

# General Firebase rules
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# Keep Hilt/Dagger classes
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }

# Keep coroutines
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# Keep reflection for Firebase
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations

# ═══════════════════════════════════════════════════════════════════════════════
# GOOGLE SIGN-IN AND AUTHENTICATION SPECIFIC RULES
# ═══════════════════════════════════════════════════════════════════════════════

# Keep AuthViewModel and all authentication related classes
-keep class com.biprangshu.chattrix.authentication.** { *; }
-keep class com.biprangshu.chattrix.authentication.AuthViewModel { *; }
-keep class com.biprangshu.chattrix.authentication.AuthState { *; }
-keep class com.biprangshu.chattrix.authentication.UpdateState { *; }

# Keep Google Sign-In classes - CRITICAL for Google authentication
-keep class com.google.android.libraries.identity.googleid.** { *; }
-keep class com.google.android.gms.auth.** { *; }
-keep class com.google.android.gms.common.** { *; }
-keep class com.google.android.gms.tasks.** { *; }

# Keep Credential Manager classes
-keep class androidx.credentials.** { *; }
-keep class androidx.credentials.exceptions.** { *; }

# Keep Google ID Token classes
-keep class com.google.android.libraries.identity.googleid.GetGoogleIdOption { *; }
-keep class com.google.android.libraries.identity.googleid.GetGoogleIdOption$Builder { *; }
-keep class com.google.android.libraries.identity.googleid.GoogleIdTokenCredential { *; }
-keep class com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException { *; }

# Keep Google Auth Provider
-keep class com.google.firebase.auth.GoogleAuthProvider { *; }

# Keep all methods in AuthViewModel - prevent obfuscation
-keepclassmembers class com.biprangshu.chattrix.authentication.AuthViewModel {
    public <methods>;
    private <methods>;
    <init>(...);
}

# Keep sealed classes for AuthState and UpdateState
-keepclassmembers class com.biprangshu.chattrix.authentication.AuthState$* {
    *;
}
-keepclassmembers class com.biprangshu.chattrix.authentication.UpdateState$* {
    *;
}

# Keep StateFlow and MutableStateFlow for AuthViewModel
-keep class kotlinx.coroutines.flow.StateFlow { *; }
-keep class kotlinx.coroutines.flow.MutableStateFlow { *; }

# Keep ViewModelScope
-keep class androidx.lifecycle.ViewModelKt { *; }

# Keep credential manager specific classes
-keep class androidx.credentials.CredentialManager { *; }
-keep class androidx.credentials.GetCredentialRequest { *; }
-keep class androidx.credentials.GetCredentialRequest$Builder { *; }
-keep class androidx.credentials.GetCredentialResponse { *; }
-keep class androidx.credentials.CustomCredential { *; }
-keep class androidx.credentials.ClearCredentialStateRequest { *; }

# Keep Google Play Services base classes
-keep class com.google.android.gms.base.** { *; }
-keep class com.google.android.gms.internal.** { *; }

# Keep Firebase User Profile classes
-keep class com.google.firebase.auth.UserProfileChangeRequest { *; }
-keep class com.google.firebase.auth.UserProfileChangeRequest$Builder { *; }

# Keep Android Context classes for Google Sign-In
-keep class android.content.Context { *; }
-keep class android.app.Application { *; }

# Keep exceptions that might be thrown during Google Sign-In
-keep class com.google.android.gms.common.api.ApiException { *; }
-keep class com.google.firebase.auth.FirebaseAuthException { *; }
-keep class com.google.firebase.auth.FirebaseAuthUserCollisionException { *; }

# Prevent obfuscation of error handling in AuthViewModel
-keepclassmembers class com.biprangshu.chattrix.authentication.AuthViewModel {
    private void handleGoogleSignInResult(...);
    private void saveUserToFirestore(...);
}


# Keep R class for string resources
-keep class com.biprangshu.chattrix.R$string { *; }

# Additional safety rules for Google Sign-In components
-keep class com.google.android.gms.signin.** { *; }
-keep class com.google.android.gms.auth.api.signin.** { *; }

# Keep all Firebase Auth listeners and callbacks
-keepclassmembers class * {
    @com.google.android.gms.tasks.OnCompleteListener *;
    @com.google.android.gms.tasks.OnSuccessListener *;
    @com.google.android.gms.tasks.OnFailureListener *;
}

# Keep Lambda expressions in AuthViewModel
-keepclassmembers class com.biprangshu.chattrix.authentication.AuthViewModel {
    synthetic lambda$*(...);
}

# Prevent warnings for missing classes
-dontwarn com.google.android.gms.**
-dontwarn androidx.credentials.**
-dontwarn com.google.android.libraries.identity.googleid.**