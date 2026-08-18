package com.ethos.rutaecologica.di

import android.content.Context
import com.ethos.rutaecologica.data.local.UserPreferencesRepository
import com.ethos.rutaecologica.data.remote.FirebaseRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseDatabase(@ApplicationContext context: Context): FirebaseDatabase {
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }
        // Agregamos la URL explícita de tu consola
        return FirebaseDatabase.getInstance("https://rutaecologicaethos-default-rtdb.firebaseio.com")
    }

    @Provides
    @Singleton
    fun provideUserPreferencesRepository(
        @ApplicationContext context: Context,
        firebaseRepository: FirebaseRepository
    ): UserPreferencesRepository = UserPreferencesRepository(context, firebaseRepository)
}