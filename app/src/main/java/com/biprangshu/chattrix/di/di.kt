package com.biprangshu.chattrix.di

import com.biprangshu.chattrix.services.ChatService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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