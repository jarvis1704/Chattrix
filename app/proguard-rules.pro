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