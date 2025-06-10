package com.biprangshu.chattrix.di

import android.content.Context
import com.biprangshu.chattrix.authentication.GoogleAuthClient
import com.biprangshu.chattrix.services.ChatService
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {
    @Provides
    fun provideFirebaseDatabase(): FirebaseDatabase {
        return FirebaseDatabase.getInstance("https://chattrix-9fbb6-default-rtdb.europe-west1.firebasedatabase.app")
    }

    @Provides
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideSignInClient(@ApplicationContext context: Context): SignInClient{
        return Identity.getSignInClient(context)
    }

    @Provides
    @Singleton
    fun provideGoogleAuthClient(
        @ApplicationContext context: Context,
        oneTapClient: SignInClient
    ): GoogleAuthClient{
        return GoogleAuthClient(context = context, oneTapClient = oneTapClient)
    }
}


@Module
@InstallIn(SingletonComponent::class)
object ChatServiceModule {
    @Provides
    @Singleton
    fun provideChatService(
        auth: FirebaseAuth,
        database: FirebaseDatabase
    ): ChatService {
        return ChatService(database, auth)
    }
}